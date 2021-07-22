package kr.co.samplepcb.xp.service;

import coolib.common.CCObjectResult;
import coolib.common.CCResult;
import coolib.common.QueryParam;
import coolib.util.CommonUtils;
import kr.co.samplepcb.xp.config.ApplicationProperties;
import kr.co.samplepcb.xp.domain.PcbKindSearch;
import kr.co.samplepcb.xp.domain.PcbPartsSearch;
import kr.co.samplepcb.xp.pojo.ElasticIndexName;
import kr.co.samplepcb.xp.pojo.PcbPartsSearchField;
import kr.co.samplepcb.xp.pojo.PcbPartsSearchVM;
import kr.co.samplepcb.xp.pojo.adapter.PagingAdapter;
import kr.co.samplepcb.xp.repository.PcbKindSearchRepository;
import kr.co.samplepcb.xp.repository.PcbPartsSearchRepository;
import kr.co.samplepcb.xp.service.common.sub.ExcelSubService;
import kr.co.samplepcb.xp.util.CoolElasticUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

@Service
public class PcbPartsService {

    private static final Logger log = LoggerFactory.getLogger(PcbPartsService.class);

    // search
    private final RestHighLevelClient restHighLevelClient;
    private final PcbPartsSearchRepository pcbPartsSearchRepository;
    private final PcbKindSearchRepository pcbKindSearchRepository;

    // service
    private final ExcelSubService excelSubService;

    // prop
    private final ApplicationProperties applicationProperties;

    public PcbPartsService(RestHighLevelClient restHighLevelClient, PcbPartsSearchRepository pcbPartsSearchRepository, PcbKindSearchRepository pcbKindSearchRepository, ExcelSubService excelSubService, ApplicationProperties applicationProperties) {
        this.restHighLevelClient = restHighLevelClient;
        this.pcbPartsSearchRepository = pcbPartsSearchRepository;
        this.pcbKindSearchRepository = pcbKindSearchRepository;
        this.excelSubService = excelSubService;
        this.applicationProperties = applicationProperties;
    }

    public CCResult indexing(PcbPartsSearchVM pcbPartsSearchVM) {

        PcbPartsSearch pcbPartsSearch = new PcbPartsSearch();
        if(StringUtils.isNotEmpty(pcbPartsSearchVM.getId())) {
            Optional<PcbPartsSearch> findPcbPartsOpt = this.pcbPartsSearchRepository.findById(pcbPartsSearchVM.getId());
            if(!findPcbPartsOpt.isPresent()) {
                return CCResult.dataNotFound();
            }
            pcbPartsSearch = findPcbPartsOpt.get();
        }
        if (pcbPartsSearchVM.getStatus() == null) {
            pcbPartsSearchVM.setStatus(0);
        }
        if(StringUtils.isNotEmpty(pcbPartsSearchVM.getToken()) // 토큰값이 있지만 값이 안맞는경우
                && !pcbPartsSearchVM.getToken().equals(applicationProperties.getAuth().getToken())
                // token 값이 없는경우
                || StringUtils.isEmpty(pcbPartsSearchVM.getToken())) {
            pcbPartsSearchVM.setStatus(0);
        }
        BeanUtils.copyProperties(pcbPartsSearchVM, pcbPartsSearch);

        return CCObjectResult.setSimpleData(this.pcbPartsSearchRepository.save(pcbPartsSearch));
    }

    public CCResult search(Pageable pageable, QueryParam queryParam, PcbPartsSearchVM pcbPartsSearchVM) {
        BoolQueryBuilder query = boolQuery();
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        QueryBuilder queryBuilder;
        if(StringUtils.isNotEmpty(queryParam.getQ())) {
            this.pcbPartsSearchRepository.makeWildcardPermitFieldQuery(queryParam.getQ(), query, highlightBuilder);
        }
        queryBuilder = this.pcbPartsSearchRepository.searchByColumnSearch(pcbPartsSearchVM, queryParam, query, highlightBuilder);

        // 상태
        List<Integer> statusList;
        if(StringUtils.isNotEmpty(pcbPartsSearchVM.getToken())
                && pcbPartsSearchVM.getToken().equals(applicationProperties.getAuth().getToken())) {
            statusList = pcbPartsSearchVM.getStatusList();
            if(statusList != null) {
                BoolQueryBuilder statusQuery = boolQuery();
                for (Integer status : statusList) {
                    statusQuery.should(matchQuery(PcbPartsSearchField.STATUS, status));
                }
                ((BoolQueryBuilder) queryBuilder).filter(statusQuery);
            }
        } else {
            ((BoolQueryBuilder) queryBuilder).filter(QueryBuilders.matchQuery(PcbPartsSearchField.STATUS, 0));
        }

        SearchResponse response = CoolElasticUtils.search(
                this.restHighLevelClient,
                new FunctionScoreQueryBuilder(queryBuilder, CoolElasticUtils.defaultGaussDecayFnBuilder(PcbPartsSearchField.WRITE_DATE)),
                highlightBuilder,
                pageable,
                null,
                request -> request.indices(ElasticIndexName.PCB_PARTS)
        );

        return PagingAdapter.toCCPagingResult(pageable, CoolElasticUtils.getSourceWithHighlightDetailInline(response), response.getHits().getTotalHits().value);
    }

    public CCResult deleteParts(List<String> ids) {
        Iterable<PcbPartsSearch> pcbPartsSearches = this.pcbPartsSearchRepository.findAllById(ids);
        this.pcbPartsSearchRepository.deleteAll(pcbPartsSearches);
        return CCResult.ok();
    }

    public void reindexAllByFile(MultipartFile file) {

        this.pcbPartsSearchRepository.deleteAll();

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
        List<PcbPartsSearch> pcbPartsSearchList = new ArrayList<>();
        List<PcbKindSearch> pcbKindSearchList = new ArrayList<>();
        Map<String, PcbPartsSearch> pcbPartsSearchMap = new HashMap<>();
        Map<Integer, Map<String, PcbKindSearch>> targetPcbKindSearchMap = new HashMap<>();

        if(rows < 1) {
            log.info("pcb parts item indexing, data rows={}", rows);
            return;
        }

        for (int rowIndex = 1; rowIndex < rows; rowIndex++) {
            XSSFRow row = sheet.getRow(rowIndex); // 각 행을 읽어온다
            if (row == null) {
                continue;
            }

            // 특수문자 제거하여 저장
            String valueStr = this.excelSubService.getCellStrValue(row, 4).trim().replaceAll("[^a-zA-Z0-9]", "");

            PcbPartsSearch findPcbItem = pcbPartsSearchMap.get(valueStr);
            if(findPcbItem != null) {
                continue;
            }
            String largeCategory = checkPcbKindExist(targetPcbKindSearchMap, row, 1, PcbPartsSearchField.LARGE_CATEGORY);
            String mediumCategory = checkPcbKindExist(targetPcbKindSearchMap, row, 2, PcbPartsSearchField.MEDIUM_CATEGORY);
            String smallCategory = checkPcbKindExist(targetPcbKindSearchMap, row, 3, PcbPartsSearchField.SMALL_CATEGORY);
            String manufacturerName = checkPcbKindExist(targetPcbKindSearchMap, row, 6, PcbPartsSearchField.MANUFACTURER_NAME);
            String packaging = checkPcbKindExist(targetPcbKindSearchMap, row, 8, PcbPartsSearchField.PACKAGING);
            String offerName = checkPcbKindExist(targetPcbKindSearchMap, row, 13, PcbPartsSearchField.OFFER_NAME);

            PcbPartsSearch pcbPartsSearch = new PcbPartsSearch();
            pcbPartsSearch.setLargeCategory(largeCategory);
            pcbPartsSearch.setMediumCategory(mediumCategory);
            pcbPartsSearch.setSmallCategory(smallCategory);
            pcbPartsSearch.setPartName(this.excelSubService.getCellStrValue(row, 4));
            pcbPartsSearch.setDescription(this.excelSubService.getCellStrValue(row, 5));
            pcbPartsSearch.setManufacturerName(manufacturerName);
            pcbPartsSearch.setPartsPackaging(this.excelSubService.getCellStrValue(row, 7));
            pcbPartsSearch.setPackaging(packaging);
            pcbPartsSearch.setMoq(this.excelSubService.getCellNumberValue(row, 9).intValue());
            pcbPartsSearch.setPrice(this.excelSubService.getCellNumberValue(row, 10).intValue());
            pcbPartsSearch.setInventoryLevel(this.excelSubService.getCellNumberValue(row, 11).intValue());
            pcbPartsSearch.setMemo(this.excelSubService.getCellStrValue(row, 12));
            pcbPartsSearch.setOfferName(offerName);
            pcbPartsSearch.setManagerPhoneNumber(this.excelSubService.getCellStrValue(row, 14));
            pcbPartsSearch.setManagerName(this.excelSubService.getCellStrValue(row, 15));
            pcbPartsSearch.setManagerEmail(this.excelSubService.getCellStrValue(row, 16));
            pcbPartsSearch.setStatus(PcbPartsSearchField.Status.APPROVED.ordinal());

            log.info("pcb parts item prepare indexing : parts name={}", valueStr);
            pcbPartsSearchList.add(pcbPartsSearch);
            pcbPartsSearchMap.put(valueStr, pcbPartsSearch);
        }


        targetPcbKindSearchMap.forEach((integer, stringPcbKindSearchMap) -> {
            stringPcbKindSearchMap.forEach((s, pcbKindSearch) -> {
                pcbKindSearchList.add(pcbKindSearch);
            });
        });

        if(pcbKindSearchList.size() > 0) {
            this.pcbKindSearchRepository.saveAll(pcbKindSearchList);
            log.info("pcb parts items, new kind items indexing");
        }

        this.pcbPartsSearchRepository.saveAll(pcbPartsSearchList);
        log.info("pcb parts items indexing");
    }

    /**
     * pcb kind 에 이미 존재 하는지 검사혀여 없으면 new kind list 넣어준다
     * @param targetPcbKindSearchMap 새로운 kind map(ref)
     * @param row 로우
     * @param rowIdx 인덱스
     * @param targetName 대상명
     * @return 로우 인덱스에서 가져온 데이터
     */
    private String checkPcbKindExist(Map<Integer, Map<String, PcbKindSearch>> targetPcbKindSearchMap, XSSFRow row, int rowIdx, String targetName) {
        String value = this.excelSubService.getCellStrValue(row, rowIdx);
        makePcbKindIfNotExist(targetPcbKindSearchMap, targetName, value);
        return value;
    }

    /**
     * pcb kind 에 존재 하지 않으면 만들어 리턴
     * @param refTargetPcbKindSearchMap target 별 value 값이 저장된 map
     * @param targetName 대성명
     * @param value 값
     */
    private void makePcbKindIfNotExist(Map<Integer, Map<String, PcbKindSearch>> refTargetPcbKindSearchMap, String targetName, String value) {
        Map<String, Integer> columnToTargetMap = PcbPartsSearchField.PCB_PART_TARGET_IDX_COLUMN;
        Integer target = columnToTargetMap.get(targetName);
        PcbKindSearch pcbKindSearch = pcbKindSearchRepository.findByItemNameKeywordAndTarget(value, target);
        if (pcbKindSearch == null) {
            // 검색엔진이 없으면 새로생성
            PcbKindSearch newKindSearch = new PcbKindSearch();
            newKindSearch.setItemName(value);
            newKindSearch.setTarget(target);
            Map<String, PcbKindSearch> pcbKindSearchMap = refTargetPcbKindSearchMap.get(target);
            if(pcbKindSearchMap == null) {
                // target 별 데이터에 없으면 생성
                pcbKindSearchMap = new HashMap<>();
                pcbKindSearchMap.put(value, newKindSearch);
                refTargetPcbKindSearchMap.put(target, pcbKindSearchMap);
            } else {
                if(pcbKindSearchMap.get(value) == null) {
                    // target 별 value 데이터에 없으면 생성
                    if(pcbKindSearchMap.size() == 0) {
                        pcbKindSearchMap = new HashMap<>();
                    }
                    pcbKindSearchMap.put(value, newKindSearch);
                }
            }
        }
    }
}
