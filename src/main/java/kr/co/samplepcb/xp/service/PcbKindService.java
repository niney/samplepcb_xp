package kr.co.samplepcb.xp.service;

import coolib.common.CCObjectResult;
import coolib.common.CCResult;
import coolib.common.QueryParam;
import coolib.util.CommonUtils;
import kr.co.samplepcb.xp.domain.PcbKindSearch;
import kr.co.samplepcb.xp.pojo.ElasticIndexName;
import kr.co.samplepcb.xp.pojo.PcbKindSearchVM;
import kr.co.samplepcb.xp.pojo.adapter.PagingAdapter;
import kr.co.samplepcb.xp.repository.PcbKindSearchRepository;
import kr.co.samplepcb.xp.repository.PcbPartsSearchRepository;
import kr.co.samplepcb.xp.service.common.sub.ExcelSubService;
import kr.co.samplepcb.xp.service.common.sub.PcbPartsSubService;
import kr.co.samplepcb.xp.util.CoolElasticUtils;
import org.apache.commons.lang3.StringUtils;
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
import java.util.*;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;

@Service
public class PcbKindService {

    private static final Logger log = LoggerFactory.getLogger(PcbKindService.class);

    // search
    private final RestHighLevelClient restHighLevelClient;
    private final PcbKindSearchRepository pcbKindSearchRepository;
    private final PcbPartsSearchRepository pcbPartsSearchRepository;
    private final ElasticsearchRestTemplate elasticsearchRestTemplate;

    // service
    private final ExcelSubService excelSubService;
    private final PcbPartsSubService pcbPartsSubService;

    public PcbKindService(RestHighLevelClient restHighLevelClient, PcbKindSearchRepository pcbKindSearchRepository, PcbPartsSearchRepository pcbPartsSearchRepository, ElasticsearchRestTemplate elasticsearchRestTemplate, ExcelSubService excelSubService, PcbPartsSubService pcbPartsSubService) {
        this.restHighLevelClient = restHighLevelClient;
        this.pcbKindSearchRepository = pcbKindSearchRepository;
        this.pcbPartsSearchRepository = pcbPartsSearchRepository;
        this.elasticsearchRestTemplate = elasticsearchRestTemplate;
        this.excelSubService = excelSubService;
        this.pcbPartsSubService = pcbPartsSubService;
    }

    public void reindexAll() {

        this.pcbKindSearchRepository.deleteAll();

        FileInputStream fis = null;
        XSSFWorkbook workbook = null;
        try {
            fis = new FileInputStream("samplepcb_kind.xlsx");
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

    @SuppressWarnings("rawtypes")
    private void excelIndexing(XSSFWorkbook workbook, int sheetAt) {
        XSSFSheet sheet = workbook.getSheetAt(sheetAt); // 해당 엑셀파일의 시트(Sheet) 수
        int rows = sheet.getPhysicalNumberOfRows(); // 해당 시트의 행의 개수
        List<PcbKindSearch> pcbKindSearchList = new ArrayList<>();
        Map<String, PcbKindSearch> pcbKindSearchMap = new HashMap<>();
        List<Map> modifyPcbKindList = new ArrayList<>();
        int target = sheetAt + 1;

        for (int rowIndex = 0; rowIndex < rows; rowIndex++) {
            XSSFRow row = sheet.getRow(rowIndex); // 각 행을 읽어온다
            if (row == null) {
                continue;
            }

            String valueStr = this.excelSubService.getCellStrValue(row, 1).trim();
            if(StringUtils.isEmpty(valueStr)) {
                continue;
            }
            PcbKindSearch findPcbItem = pcbKindSearchMap.get(valueStr);
            if(findPcbItem != null) {
                continue;
            }
            findPcbItem = this.pcbKindSearchRepository.findByItemNameKeywordAndTarget(valueStr, target);
            if(findPcbItem != null) {
                continue;
            }

            PcbKindSearch pcbKindSearch = new PcbKindSearch();
            String id = this.excelSubService.getCellStrValue(row, 0).trim();
            if(StringUtils.isNotEmpty(id)) {
                Optional<PcbKindSearch> findPcbKindOpt = this.pcbKindSearchRepository.findById(id);
                if (findPcbKindOpt.isPresent()) {
                    pcbKindSearch = findPcbKindOpt.get();
                    if(!pcbKindSearch.getItemName().equals(valueStr)) {
                        Map<Object, Object> modifyInfo  = new HashMap<>();
                        modifyInfo.put("target", pcbKindSearch.getTarget());
                        modifyInfo.put("from", pcbKindSearch.getItemName());
                        modifyInfo.put("to", valueStr);
                        modifyPcbKindList.add(modifyInfo);
                    }
                } else {
                    pcbKindSearch.setId(id);
                }
            }
            pcbKindSearch.setItemName(valueStr);
            pcbKindSearch.setTarget(target);

            log.info("pcb kind item prepare indexing : target={}, value={}", target, valueStr);
            pcbKindSearchList.add(pcbKindSearch);
            pcbKindSearchMap.put(valueStr, pcbKindSearch);
        }

        for (Map modifyPcbKind : modifyPcbKindList) {
            Integer modifyTarget = (Integer) modifyPcbKind.get("target");
            String from = (String) modifyPcbKind.get("from");
            String to = (String) modifyPcbKind.get("to");
            CCResult result = this.pcbPartsSubService.updateKindAllByGroup(modifyTarget, from, to);
            if(!result.isResult()) {
                log.error("pcb modify kind parts indexing error : target={}, form={}, to={}, msg={}", modifyTarget, from, to, result.getMessage());
                return;
            }
            log.info("pcb modify kind parts indexing : target={}, form={}, to={}", modifyTarget, from, to);
        }

        this.pcbKindSearchRepository.saveAll(pcbKindSearchList);
        log.info("pcb kind items indexing : target={}", target);
    }

    public CCResult search(Pageable pageable, QueryParam queryParam, PcbKindSearchVM pcbKindSearchVM) {
        BoolQueryBuilder query = boolQuery();
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        QueryBuilder queryBuilder = this.pcbKindSearchRepository.searchByItemSearch(pcbKindSearchVM, queryParam, query, highlightBuilder);

        SearchResponse response = CoolElasticUtils.search(
                this.restHighLevelClient,
                queryBuilder,
                highlightBuilder,
                pageable,
                null,
                request -> request.indices(ElasticIndexName.PCB_KIND)
        );

        return PagingAdapter.toCCPagingResult(pageable, CoolElasticUtils.getSourceWithHighlightDetailInline(response), response.getHits().getTotalHits().value);
    }

    public CCResult indexing(PcbKindSearchVM pcbKindSearchVM) {
        PcbKindSearch pcbKindSearch = this.pcbKindSearchRepository.findByItemNameKeywordAndTarget(pcbKindSearchVM.getItemName(), pcbKindSearchVM.getTarget());

        if(pcbKindSearch != null) {
            CCResult ccResult = new CCResult();
            ccResult.setResult(false);
            ccResult.setMessage("동일한 아이템명 존재합니다.");
            return ccResult;
        }

        pcbKindSearch = new PcbKindSearch();
        if(pcbKindSearchVM.getId() != null) {
            Optional<PcbKindSearch> findPcbKindOpt = this.pcbKindSearchRepository.findById(pcbKindSearchVM.getId());
            if(!findPcbKindOpt.isPresent()) {
                return CCResult.dataNotFound();
            }
            pcbKindSearch = findPcbKindOpt.get();
            CCResult result = this.pcbPartsSubService.updateKindAllByGroup(pcbKindSearchVM.getTarget(),
                    pcbKindSearch.getItemName(), // 기존명
                    pcbKindSearchVM.getItemName()); // 수정명
            if(!result.isResult()) {
                return result;
            }
        }
        BeanUtils.copyProperties(pcbKindSearchVM, pcbKindSearch);

        this.pcbKindSearchRepository.save(pcbKindSearch);

        return CCObjectResult.setSimpleData(pcbKindSearch);
    }

    public CCResult delete(String id) {
        Optional<PcbKindSearch> kindOpt = this.pcbKindSearchRepository.findById(id);
        if(!kindOpt.isPresent()) {
            return CCResult.dataNotFound();
        }
        this.pcbKindSearchRepository.deleteById(id);
        return CCResult.ok();
    }

    public List<List<PcbKindSearchVM>> getAllItemGroupByTarget() {
        List<List<PcbKindSearchVM>> pcbKindLists = new ArrayList<>();
        for (int target = 1; target <= 6; target++) {
            Iterable<PcbKindSearch> kindSearches = this.pcbKindSearchRepository.findAllByTarget(target);
            List<PcbKindSearchVM> pcbKindSearchVMList = new ArrayList<>();
            kindSearches.forEach(pcbItemSearch -> {
                PcbKindSearchVM item = new PcbKindSearchVM();
                BeanUtils.copyProperties(pcbItemSearch, item);
                pcbKindSearchVMList.add(item);
            });
            pcbKindLists.add(pcbKindSearchVMList);
        }
        return pcbKindLists;
    }
}
