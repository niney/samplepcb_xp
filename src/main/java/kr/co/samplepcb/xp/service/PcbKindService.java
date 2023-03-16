package kr.co.samplepcb.xp.service;

import coolib.common.CCObjectResult;
import coolib.common.CCResult;
import coolib.common.QueryParam;
import coolib.util.CommonUtils;
import kr.co.samplepcb.xp.config.ApplicationProperties;
import kr.co.samplepcb.xp.domain.PcbKindSearch;
import kr.co.samplepcb.xp.pojo.ElasticIndexName;
import kr.co.samplepcb.xp.pojo.PcbKindSearchVM;
import kr.co.samplepcb.xp.pojo.PcbPartsSearchField;
import kr.co.samplepcb.xp.pojo.adapter.PagingAdapter;
import kr.co.samplepcb.xp.pojo.octopart.OctoPartManufacturers;
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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;

@Service
public class PcbKindService {

    private static final Logger log = LoggerFactory.getLogger(PcbKindService.class);

    private final String mlServerUrl;

    // search
    private final RestHighLevelClient restHighLevelClient;
    private final PcbKindSearchRepository pcbKindSearchRepository;
    private final PcbPartsSearchRepository pcbPartsSearchRepository;
    private final ElasticsearchRestTemplate elasticsearchRestTemplate;

    // service
    private final ExcelSubService excelSubService;
    private final PcbPartsSubService pcbPartsSubService;

    public PcbKindService(ApplicationProperties appProp, RestHighLevelClient restHighLevelClient, PcbKindSearchRepository pcbKindSearchRepository, PcbPartsSearchRepository pcbPartsSearchRepository, ElasticsearchRestTemplate elasticsearchRestTemplate, ExcelSubService excelSubService, PcbPartsSubService pcbPartsSubService) {
        // prop
        this.mlServerUrl = appProp.getMlServer().getServerUrl();
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
            // 1,2,3 대중소 분류는 패스
            for (int i = 2; i < workbook.getNumberOfSheets(); i++) {
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

    public void reindexAllByFileForCategory(MultipartFile file) {

        XSSFWorkbook workbook = null;
        try {
            workbook = new XSSFWorkbook(file.getInputStream());
            excelIndexingForCategory(workbook, 0);
        } catch (Exception e) {
            log.error(CommonUtils.getFullStackTrace(e));
        } finally {
            try {
                if (workbook != null) {
                    workbook.close();
                }
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void excelIndexingForCategory(XSSFWorkbook workbook, int sheetAt) {
        XSSFSheet sheet = workbook.getSheetAt(sheetAt); // 해당 엑셀파일의 시트(Sheet) 수
        int rows = sheet.getPhysicalNumberOfRows(); // 해당 시트의 행의 개수

        Map<PcbKindSearch, Object> firstMap = new HashMap();
        PcbKindSearch firstKind = null;

        Map<PcbKindSearch, Object> secondMap = new HashMap<>();
        PcbKindSearch secondKind = null;

        Map<PcbKindSearch, Object> thirdMap = new HashMap<>();

        // 카테고리 분류(타켓) 삭제
        this.pcbKindSearchRepository.deleteByTarget(1);
        this.pcbKindSearchRepository.deleteByTarget(2);
        this.pcbKindSearchRepository.deleteByTarget(3);

        for (int rowIndex = 0; rowIndex < rows; rowIndex++) {
            XSSFRow row = sheet.getRow(rowIndex); // 각 행을 읽어온다
            if (row == null) {
                continue;
            }

            String firstItemName = this.excelSubService.getCellStrValue(row, 0).trim();
            if(StringUtils.isNotEmpty(firstItemName)) {
                firstKind = new PcbKindSearch();
                firstKind.setTarget(1);
                firstKind.setItemName(firstItemName);
                firstKind.setDisplayName(this.excelSubService.getCellStrValue(row, 1).trim());
                firstMap.put(firstKind, null);
            }

            String secondItemName = this.excelSubService.getCellStrValue(row, 2).trim();
            if (StringUtils.isNotEmpty(secondItemName)) {
                secondKind = new PcbKindSearch();
                secondKind.setTarget(2);
                secondKind.setItemName(secondItemName);
                secondKind.setDisplayName(this.excelSubService.getCellStrValue(row, 3).trim());

                if(firstMap.get(firstKind) == null) {
                    secondMap = new HashMap<>();
                    firstMap.put(firstKind, secondMap);
                }
                secondMap.put(secondKind, null);
            }

            String thirdItemName = this.excelSubService.getCellStrValue(row, 4).trim();
            if (StringUtils.isNotEmpty(thirdItemName)) {
                PcbKindSearch thirdKind = new PcbKindSearch();
                thirdKind.setTarget(3);
                thirdKind.setItemName(thirdItemName);
                thirdKind.setDisplayName(this.excelSubService.getCellStrValue(row, 5).trim());

                if(secondMap.get(secondKind) == null) {
                    thirdMap = new HashMap<>();
                    secondMap.put(secondKind, thirdMap);
                }
                thirdMap.put(thirdKind, null);
            }
        }

        this.saveCategoryList(firstMap);
        log.info("pcb kind category items indexing");
    }

    @SuppressWarnings("unchecked")
    private void saveCategoryList(Map<PcbKindSearch, Object> firstMap) {
        // 저장
        List<PcbKindSearch> savePcbKindSearchList = new ArrayList<>();
        firstMap.forEach((pcbKindSearch1, map1) -> {
            savePcbKindSearchList.add(pcbKindSearch1);
            if (map1 instanceof HashMap) {
                Map<PcbKindSearch, Object> secondMap = (Map<PcbKindSearch, Object>) map1;
                secondMap.forEach((pcbKindSearch2, map2) -> {
                    savePcbKindSearchList.add(pcbKindSearch2);
                    if (map2 instanceof HashMap) {
                        Map<PcbKindSearch, Object> thirdMap = (Map<PcbKindSearch, Object>) map2;
                        thirdMap.forEach((pcbKindSearch3, map3) -> {
                            savePcbKindSearchList.add(pcbKindSearch3);
                        });
                    }
                });
            }
        });
        this.pcbKindSearchRepository.saveAll(savePcbKindSearchList);

        // pId 를 업데이트 하여 저장
        firstMap.forEach((pcbKindSearch1, map1) -> {
            if (map1 instanceof HashMap) {
                Map<PcbKindSearch, Object> secondMap = (Map<PcbKindSearch, Object>) map1;
                secondMap.forEach((pcbKindSearch2, map2) -> {
                    pcbKindSearch2.setpId(pcbKindSearch1.getId());
                    if (map2 instanceof HashMap) {
                        Map<PcbKindSearch, Object> thirdMap = (Map<PcbKindSearch, Object>) map2;
                        thirdMap.forEach((pcbKindSearch3, map3) -> {
                            pcbKindSearch3.setpId(pcbKindSearch2.getId());
                        });
                    }
                });
            }
        });
        this.pcbKindSearchRepository.saveAll(savePcbKindSearchList);
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
                searchSourceBuilder -> {
                    searchSourceBuilder.trackTotalHits(true);
                },
                request -> request.indices(ElasticIndexName.PCB_KIND)
        );

        return PagingAdapter.toCCPagingResult(pageable, CoolElasticUtils.getSourceWithHighlightDetailInline(response), response.getHits().getTotalHits().value);
    }

    private CCResult prepareIndexing(PcbKindSearchVM pcbKindSearchVM) {
        CCResult ccResult = new CCResult();

        PcbKindSearch pcbKindSearch = this.pcbKindSearchRepository.findByItemNameKeywordAndTarget(pcbKindSearchVM.getItemName(), pcbKindSearchVM.getTarget());
        if (pcbKindSearch != null && (pcbKindSearch.getDisplayName() == null || pcbKindSearch.getDisplayName().equals(pcbKindSearchVM.getDisplayName()))) {
            ccResult.setResult(false);
            ccResult.setMessage("동일한 아이템명 존재합니다.");
            return ccResult;
        }

        pcbKindSearch = new PcbKindSearch();
        if (pcbKindSearchVM.getId() != null) {
            Optional<PcbKindSearch> findPcbKindOpt = this.pcbKindSearchRepository.findById(pcbKindSearchVM.getId());
            if (!findPcbKindOpt.isPresent()) {
                return CCResult.dataNotFound();
            }
            pcbKindSearch = findPcbKindOpt.get();
            CCResult result = this.pcbPartsSubService.updateKindAllByGroup(pcbKindSearchVM.getTarget(),
                    pcbKindSearch.getItemName(), // 기존명
                    pcbKindSearchVM.getItemName()); // 수정명
            if (!result.isResult()) {
                return result;
            }
        }
        return CCObjectResult.setSimpleData(pcbKindSearch);
    }

    @SuppressWarnings("unchecked")
    public CCResult indexing(PcbKindSearchVM pcbKindSearchVM) {
        CCResult ccResult = this.prepareIndexing(pcbKindSearchVM);
        if (!ccResult.getResult()) {
            log.warn("pcb kind indexing warn name: {}, target: {}", pcbKindSearchVM.getItemName(), pcbKindSearchVM.getTarget());
            return ccResult;
        }
        if (!(ccResult instanceof CCObjectResult)) {
            return ccResult;
        }
        PcbKindSearch pcbKindSearch = ((CCObjectResult<PcbKindSearch>) ccResult).getData();
        BeanUtils.copyProperties(pcbKindSearchVM, pcbKindSearch);

        this.pcbKindSearchRepository.save(pcbKindSearch);
        log.info("pcb kind indexing name: {}, target: {}", pcbKindSearch.getItemName(), pcbKindSearch.getTarget());

        return CCObjectResult.setSimpleData(pcbKindSearch);
    }

    @SuppressWarnings("unchecked")
    public CCResult indexing(List<PcbKindSearchVM> pcbKindSearchVMList) {

        List<PcbKindSearch> pcbKindSearchList = new ArrayList<>();
        for (PcbKindSearchVM pcbKindSearchVM : pcbKindSearchVMList) {
            CCResult ccResult = this.prepareIndexing(pcbKindSearchVM);
            if(!ccResult.getResult()) {
                log.warn("pcb kind indexing warn name: {}, target: {}, message: {}", pcbKindSearchVM.getItemName(), pcbKindSearchVM.getTarget(), ccResult.getMessage());
                continue;
            }
            if(!(ccResult instanceof CCObjectResult)) {
                continue;
            }
            PcbKindSearch pcbKindSearch = ((CCObjectResult<PcbKindSearch>) ccResult).getData();
            BeanUtils.copyProperties(pcbKindSearchVM, pcbKindSearch);
            log.info("pcb kind prepare indexing name: {}, target: {}", pcbKindSearch.getItemName(), pcbKindSearch.getTarget());
            pcbKindSearchList.add(pcbKindSearch);
        }

        this.pcbKindSearchRepository.saveAll(pcbKindSearchList);
        log.info("pcb kind prepare indexing complete");

        return CCResult.ok();
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
        return this.getAllItemGroupByTarget(PcbPartsSearchField.PCB_PART_COLUMN_IDX_TARGET.length - 1);
    }

    public List<List<PcbKindSearchVM>> getAllItemGroupByTarget(int length) {
        List<List<PcbKindSearchVM>> pcbKindLists = new ArrayList<>();
        for (int target = 1; target <= length; target++) {
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

    public List<OctoPartManufacturers> getRequestManufacturers() {

        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 10 * 1000)).build();
        WebClient webClient = WebClient.builder().exchangeStrategies(exchangeStrategies).build();

        Mono<Map<String, List<OctoPartManufacturers>>> requestSpec = webClient
                .method(HttpMethod.GET)
                .uri(mlServerUrl + "/api/searchManufacturers")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, List<OctoPartManufacturers>>>() {
                });

        Map<String, List<OctoPartManufacturers>> listMap = requestSpec.block();
        return listMap.get("manufacturers");
    }

    public CCResult indexingManufacturers() {
        Integer target = PcbPartsSearchField.PCB_PART_TARGET_IDX_COLUMN.get(PcbPartsSearchField.MANUFACTURER_NAME);
        List<OctoPartManufacturers> manufacturers = this.getRequestManufacturers();
        List<PcbKindSearchVM> pcbKindSearchVMList = new ArrayList<>();

        for (OctoPartManufacturers manufacturer : manufacturers) {
            PcbKindSearchVM pcbKindSearchVM = new PcbKindSearchVM();
            pcbKindSearchVM.setItemName(manufacturer.getName());
            pcbKindSearchVM.setEtc1(manufacturer.getId());
            pcbKindSearchVM.setEtc2(manufacturer.getSlug());
            pcbKindSearchVM.setEtc3(manufacturer.getHomepageUrl());
            pcbKindSearchVM.setTarget(target);

            pcbKindSearchVMList.add(pcbKindSearchVM);
        }

        return this.indexing(pcbKindSearchVMList);
    }
}
