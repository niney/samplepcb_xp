package kr.co.samplepcb.xp.service;

import coolib.common.CCObjectResult;
import coolib.common.CCResult;
import coolib.common.QueryParam;
import coolib.util.CommonUtils;
import kr.co.samplepcb.xp.domain.PcbItemSearch;
import kr.co.samplepcb.xp.pojo.ElasticIndexName;
import kr.co.samplepcb.xp.pojo.PcbItemSearchVM;
import kr.co.samplepcb.xp.pojo.adapter.PagingAdapter;
import kr.co.samplepcb.xp.repository.PcbItemSearchRepository;
import kr.co.samplepcb.xp.util.CoolElasticUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;

@Service
public class PcbItemService {

    private static final Logger log = LoggerFactory.getLogger(PcbItemService.class);

    // search
    private final RestHighLevelClient restHighLevelClient;
    private final PcbItemSearchRepository pcbItemSearchRepository;
    private final ElasticsearchRestTemplate elasticsearchRestTemplate;

    public PcbItemService(RestHighLevelClient restHighLevelClient, PcbItemSearchRepository pcbItemSearchRepository, ElasticsearchRestTemplate elasticsearchRestTemplate) {
        this.restHighLevelClient = restHighLevelClient;
        this.pcbItemSearchRepository = pcbItemSearchRepository;
        this.elasticsearchRestTemplate = elasticsearchRestTemplate;
    }

    public void reindexAll() {

        this.pcbItemSearchRepository.deleteAll();

        FileInputStream fis = null;
        XSSFWorkbook workbook = null;
        try {
            fis = new FileInputStream("samplepcb_bom.xlsx");
            workbook = new XSSFWorkbook(fis);
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                excelIndexing(workbook, i);
            }
        } catch (Exception e) {
            log.error(CommonUtils.getFullStackTrace(e));
        } finally {
            try {
                if(fis != null) {
                    fis.close();
                }
                if(workbook != null) {
                    workbook.close();
                }
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }

    public void reindexAllByFile(MultipartFile file) {

        this.pcbItemSearchRepository.deleteAll();

        XSSFWorkbook workbook = null;
        try {
            workbook = new XSSFWorkbook(file.getInputStream());
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                excelIndexing(workbook, i);
            }
        } catch (Exception e) {
            log.error(CommonUtils.getFullStackTrace(e));
        } finally {
            try {
                if(workbook != null) {
                    workbook.close();
                }
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }

    private void excelIndexing(XSSFWorkbook workbook, int sheetAt) {
        XSSFSheet sheet = workbook.getSheetAt(sheetAt); // 해당 엑셀파일의 시트(Sheet) 수
        int rows = sheet.getPhysicalNumberOfRows(); // 해당 시트의 행의 개수
        List<PcbItemSearch> pcbItemSearchList = new ArrayList<>();
        Map<String, PcbItemSearch> pcbItemSearchMap = new HashMap<>();

        for (int rowIndex = 1; rowIndex < rows; rowIndex++) {
            XSSFRow row = sheet.getRow(rowIndex); // 각 행을 읽어온다
            if (row == null) {
                continue;
            }

            String valueStr = getCellStrValue(row, 0).trim();
            int target = Integer.parseInt(getCellStrValue(row, 1));

            PcbItemSearch findPcbItem = pcbItemSearchMap.get(valueStr);
            if(findPcbItem != null) {
                continue;
            }

            PcbItemSearch pcbItemSearch = new PcbItemSearch();
            pcbItemSearch.setItemName(valueStr);
            pcbItemSearch.setItemNameText(valueStr);
            pcbItemSearch.setTarget(sheetAt + 1);

            log.info("pcb item prepare indexing : target={}, value={}", target, valueStr);
            pcbItemSearchList.add(pcbItemSearch);
            pcbItemSearchMap.put(valueStr, pcbItemSearch);
        }

        this.pcbItemSearchRepository.saveAll(pcbItemSearchList);
        log.info("pcb items indexing : target={}", sheetAt + 1);
    }

    private String getCellStrValue(XSSFRow row, int columnIndex) {
        String valueStr = "";
        XSSFCell cell = row.getCell(columnIndex); // 셀에 담겨있는 값을 읽는다.
        if(cell == null) {
            return "";
        }
        switch (cell.getCellType()) { // 각 셀에 담겨있는 데이터의 타입을 체크하고 해당 타입에 맞게 가져온다.
            case NUMERIC:
                valueStr = Integer.toString((int) cell.getNumericCellValue());
                break;
            case STRING:
                valueStr = cell.getStringCellValue() + "";
                break;
            case BLANK:
                valueStr = cell.getBooleanCellValue() + "";
                break;
            case ERROR:
                valueStr = cell.getErrorCellValue() + "";
                break;
        }
        return valueStr;
    }

    public CCResult search(Pageable pageable, QueryParam queryParam, PcbItemSearchVM pcbColumnSearchVM) {
        BoolQueryBuilder query = boolQuery();
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        QueryBuilder queryBuilder = this.pcbItemSearchRepository.searchByColumnSearch(pcbColumnSearchVM, queryParam, query, highlightBuilder);

        SearchResponse response = CoolElasticUtils.search(
                this.restHighLevelClient,
                queryBuilder,
                highlightBuilder,
                pageable,
                null,
                request -> request.indices(ElasticIndexName.PCB_ITEM)
        );

        return PagingAdapter.toCCPagingResult(pageable, CoolElasticUtils.getSourceWithHighlightDetailInline(response), response.getHits().getTotalHits().value);
    }

    public CCResult indexing(PcbItemSearchVM pcbItemSearchVM) {
        PcbItemSearch pcbItemSearch = this.pcbItemSearchRepository.findByItemNameAndTarget(pcbItemSearchVM.getItemName(), pcbItemSearchVM.getTarget());

        if(pcbItemSearch != null) {
            CCResult ccResult = new CCResult();
            ccResult.setResult(false);
            ccResult.setMessage("동일한 아이템명 존재합니다.");
            return ccResult;
        }

        pcbItemSearch = new PcbItemSearch();
        BeanUtils.copyProperties(pcbItemSearchVM, pcbItemSearch);
        pcbItemSearch.setItemNameText(pcbItemSearchVM.getItemName());

        this.pcbItemSearchRepository.save(pcbItemSearch);

        return CCObjectResult.setSimpleData(pcbItemSearch);
    }

    public CCResult delete(String id) {
        this.pcbItemSearchRepository.deleteById(id);
        return CCResult.ok();
    }

    public List<List<PcbItemSearchVM>> getAllItemGroupByTarget() {
        List<List<PcbItemSearchVM>> pcbItemLists = new ArrayList<>();
        for (int target = 1; target <= 6; target++) {
            Iterable<PcbItemSearch> itemSearches = this.pcbItemSearchRepository.findAllByTarget(target);
            List<PcbItemSearchVM> pcbItemSearchVMList = new ArrayList<>();
            itemSearches.forEach(pcbItemSearch -> {
                PcbItemSearchVM item = new PcbItemSearchVM();
                BeanUtils.copyProperties(pcbItemSearch, item);
                pcbItemSearchVMList.add(item);
            });
            pcbItemLists.add(pcbItemSearchVMList);
        }
        return pcbItemLists;
    }
}
