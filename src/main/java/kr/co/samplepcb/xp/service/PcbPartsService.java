package kr.co.samplepcb.xp.service;

import coolib.common.CCObjectResult;
import coolib.common.CCResult;
import coolib.common.QueryParam;
import coolib.util.CommonUtils;
import kr.co.samplepcb.xp.config.ApplicationProperties;
import kr.co.samplepcb.xp.domain.PcbKindSearch;
import kr.co.samplepcb.xp.domain.PcbPartsSearch;
import kr.co.samplepcb.xp.domain.PcbUnitSearch;
import kr.co.samplepcb.xp.pojo.*;
import kr.co.samplepcb.xp.pojo.adapter.PagingAdapter;
import kr.co.samplepcb.xp.repository.PcbKindSearchRepository;
import kr.co.samplepcb.xp.repository.PcbPartsSearchRepository;
import kr.co.samplepcb.xp.service.common.sub.DataExtractorSubService;
import kr.co.samplepcb.xp.service.common.sub.ExcelSubService;
import kr.co.samplepcb.xp.util.CoolElasticUtils;
import kr.co.samplepcb.xp.util.PcbPartsUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
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
    private final String samplepcbToken;
    private final DataExtractorSubService dataExtractorSubService;

    public PcbPartsService(RestHighLevelClient restHighLevelClient, PcbPartsSearchRepository pcbPartsSearchRepository, PcbKindSearchRepository pcbKindSearchRepository, ExcelSubService excelSubService, DataExtractorSubService dataExtractorSubService, ApplicationProperties applicationProperties) {
        this.restHighLevelClient = restHighLevelClient;
        this.pcbPartsSearchRepository = pcbPartsSearchRepository;
        this.pcbKindSearchRepository = pcbKindSearchRepository;
        this.excelSubService = excelSubService;
        this.dataExtractorSubService = dataExtractorSubService;
        this.applicationProperties = applicationProperties;
        this.samplepcbToken = applicationProperties.getAuth().getSamplepcbSiteToken();

    }

    public CCResult indexing(PcbPartsSearchVM pcbPartsSearchVM) {

        boolean isPermitSamePartName;
        if (StringUtils.isEmpty(pcbPartsSearchVM.getServiceType())) {
            pcbPartsSearchVM.setServiceType("");
        }
        switch (pcbPartsSearchVM.getServiceType()) {
            case "openMarket":
                isPermitSamePartName = true;
                break;
            default:
                isPermitSamePartName = false;
                break;
        }

        PcbPartsSearch pcbPartsSearch = new PcbPartsSearch();
        if(StringUtils.isNotEmpty(pcbPartsSearchVM.getId())) {
            Optional<PcbPartsSearch> findPcbPartsOpt = this.pcbPartsSearchRepository.findById(pcbPartsSearchVM.getId());
            if(!findPcbPartsOpt.isPresent()) {
                return CCResult.dataNotFound();
            }
            pcbPartsSearch = findPcbPartsOpt.get();
        } else {
            if(!isPermitSamePartName) {
                PcbPartsSearch findPcbParts = this.pcbPartsSearchRepository.findByPartNameNormalizeAndMemberId(pcbPartsSearchVM.getPartName(), pcbPartsSearchVM.getMemberId());
                if (findPcbParts != null) {
                    CCResult ccResult = new CCResult();
                    ccResult.setResult(false);
                    ccResult.setMessage("동일한 상품을 등록할 수 없습니다.");
                    return ccResult;
                }
            }
        }
        if (pcbPartsSearchVM.getStatus() == null) {
            pcbPartsSearchVM.setStatus(0);
        }

        switch (pcbPartsSearchVM.getServiceType()) {
            case "openMarket":
                // 오픈마켓은 일단 승인처리
                pcbPartsSearchVM.setStatus(1);
                break;
            default:
                if (StringUtils.isNotEmpty(pcbPartsSearchVM.getToken()) // 토큰값이 있지만 값이 안맞는경우
                        && !pcbPartsSearchVM.getToken().equals(applicationProperties.getAuth().getToken())
                        // token 값이 없는경우
                        || StringUtils.isEmpty(pcbPartsSearchVM.getToken())) {
                    pcbPartsSearchVM.setStatus(0);
                }
                break;
        }
        if (StringUtils.isEmpty(pcbPartsSearchVM.getServiceType())) {
            pcbPartsSearchVM.setServiceType(null);
        }
        BeanUtils.copyProperties(pcbPartsSearchVM, pcbPartsSearch);

        return CCObjectResult.setSimpleData(this.pcbPartsSearchRepository.save(pcbPartsSearch));
    }

    @SuppressWarnings({"rawtypes"})
    public CCResult search(Pageable pageable, QueryParam queryParam, PcbPartsSearchVM pcbPartsSearchVM) {
        BoolQueryBuilder query = boolQuery();
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        BoolQueryBuilder queryBuilder;
        if(StringUtils.isNotEmpty(queryParam.getQ()) && !queryParam.getQf().equals("parsing")) {
            this.pcbPartsSearchRepository.makeWildcardPermitFieldQuery(queryParam.getQf(), queryParam.getQ(), query, highlightBuilder);
        }
        queryBuilder = this.pcbPartsSearchRepository.searchByColumnSearch(pcbPartsSearchVM, queryParam, query, highlightBuilder);

        // parsing
        if (StringUtils.isNotEmpty(queryParam.getQf()) && queryParam.getQf().equals("parsing")) {
            boolean hasSize = false;
            BoolQueryBuilder shouldQuery = boolQuery();
            String q = queryParam.getQ();
            shouldQuery.should(matchQuery(PcbPartsSearchField.PART_NAME, q));
            highlightBuilder.field(new HighlightBuilder.Field(PcbPartsSearchField.PART_NAME));
            Map<String, List<String>> parsedKeywords = PcbPartsUtils.parseString(queryParam.getQ());
            parsedKeywords.forEach((k, v) -> {
                String keywords = String.join(" ", v);
                switch (k) {
                    case PcbPartsSearchField.WATT:
                        for (String pcb : PcbPartsSearchField.WATT_LIST) {
                            shouldQuery.should(matchQuery(pcb, keywords));
                            highlightBuilder.field(new HighlightBuilder.Field(pcb));
                        }
                        break;
                    case PcbPartsSearchField.TOLERANCE:
                        for (String pcb : PcbPartsSearchField.TOLERANCE_LIST) {
                            shouldQuery.should(matchQuery(pcb, keywords));
                            highlightBuilder.field(new HighlightBuilder.Field(pcb));
                        }
                        break;
                    case PcbPartsSearchField.OHM:
                        for (String pcb : PcbPartsSearchField.OHM_LIST) {
                            shouldQuery.should(matchQuery(pcb, keywords));
                            highlightBuilder.field(new HighlightBuilder.Field(pcb));
                        }
                        break;
                    case PcbPartsSearchField.CONDENSER:
                        for (String pcb : PcbPartsSearchField.CONDENSER_LIST) {
                            shouldQuery.should(matchQuery(pcb, keywords));
                            highlightBuilder.field(new HighlightBuilder.Field(pcb));
                        }
                        break;
                    case PcbPartsSearchField.VOLTAGE:
                        for (String pcb : PcbPartsSearchField.VOLTAGE_LIST) {
                            shouldQuery.should(matchQuery(pcb, keywords));
                            highlightBuilder.field(new HighlightBuilder.Field(pcb));
                        }
                        break;
                    case PcbPartsSearchField.CURRENT:
                        for (String pcb : PcbPartsSearchField.CURRENT_LIST) {
                            shouldQuery.should(matchQuery(pcb, keywords));
                            highlightBuilder.field(new HighlightBuilder.Field(pcb));
                        }
                        break;
                    case PcbPartsSearchField.INDUCTOR:
                        for (String pcb : PcbPartsSearchField.INDUCTOR_LIST) {
                            shouldQuery.should(matchQuery(pcb, keywords));
                            highlightBuilder.field(new HighlightBuilder.Field(pcb));
                        }
                        break;
                }
            });
            // size
            String extractedSize = this.dataExtractorSubService.extractSizeFromTitle(queryParam.getQ());
            if (parsedKeywords.get(PcbPartsSearchField.SIZE) == null && StringUtils.isNotEmpty(extractedSize)) {
                shouldQuery.should(matchQuery(PcbPartsSearchField.SIZE, extractedSize));
                highlightBuilder.field(new HighlightBuilder.Field(PcbPartsSearchField.SIZE));
            }
            queryBuilder.must(shouldQuery);
        }

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
                queryBuilder.filter(statusQuery);
            }
        } else if (StringUtils.isEmpty(pcbPartsSearchVM.getMemberId())) { // member id 가 비어있으면 filter 걸기
            queryBuilder.filter(QueryBuilders.matchQuery(PcbPartsSearchField.STATUS, 1));
        }

        SearchResponse response = CoolElasticUtils.search(
                this.restHighLevelClient,
                pageable.getSort().getOrderFor(PcbPartsSearchField.WRITE_DATE) == null
                        ? queryBuilder : new FunctionScoreQueryBuilder(queryBuilder, CoolElasticUtils.defaultGaussDecayFnBuilder(PcbPartsSearchField.WRITE_DATE)),
                highlightBuilder,
                pageable,
                searchSourceBuilder -> {
                    searchSourceBuilder.trackTotalHits(true);
                },
                request -> request.indices(ElasticIndexName.PCB_PARTS)
        );
        List<Map<String, Object>> respData = CoolElasticUtils.getSourceWithHighlightDetailInline(response);

        if(!StringUtils.isEmpty(pcbPartsSearchVM.getId()) && respData.size() == 1) {
            Map<String, Object> pcbParts = respData.get(0);
            String memberId = (String) pcbParts.get(PcbPartsSearchField.MEMBER_ID);
            if (StringUtils.isNotEmpty(memberId)) {
                // id 가 있는경우 담당자정보도 넣어준다
                RestTemplate restTemplate = new RestTemplate();
                URI uri = UriComponentsBuilder.newInstance()
                        .scheme("http").host("samplepcb.co.kr").path("/shop/member_api.php")
                        .queryParam("w", "mir") // member info read
                        .queryParam("id", memberId)
                        .queryParam("token", samplepcbToken)
                        .build().toUri();
                CCObjectResult<Map> memberResult = WebClient.create()
                        .get()
                        .uri(uri)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<CCObjectResult<Map>>() {
                        }).block();
                if (memberResult != null && memberResult.getData() != null) {
                    Map member = memberResult.getData();
                    String memberName = (String) member.get("mb_name");
                    String memberTel = (String) member.get("mb_tel");
                    String memberEmail = (String) member.get("mb_email");
                    String managerName = (String) member.get("mb_7");
                    String managerTel = (String) member.get("mb_9");
                    String managerEmail = (String) member.get("mb_15");
                    pcbParts.put(PcbPartsSearchField.CURRENT_MEMBER_NAME, memberName);
                    pcbParts.put(PcbPartsSearchField.CURRENT_MEMBER_PHONE_NUMBER, memberTel);
                    pcbParts.put(PcbPartsSearchField.CURRENT_MEMBER_EMAIL, memberEmail);
                    pcbParts.put(PcbPartsSearchField.CURRENT_MANAGER_NAME, managerName);
                    pcbParts.put(PcbPartsSearchField.CURRENT_MANAGER_PHONE_NUMBER, managerTel);
                    pcbParts.put(PcbPartsSearchField.CURRENT_MANAGER_EMAIL, managerEmail);
                }
            }
        }

        return PagingAdapter.toCCPagingResult(pageable, respData, response.getHits().getTotalHits().value);
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

    public void reindexAllByOfferName(MultipartFile file, String offerName) {
        this.pcbPartsSearchRepository.deleteAllByOfferName(offerName);

        XSSFWorkbook workbook = null;
        try {
            workbook = new XSSFWorkbook(file.getInputStream());
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                excelIndexingByOfferName(workbook, i, offerName);
            }
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
            String largeCategory = checkPcbKindExistForCategory(row, 1, PcbPartsSearchField.LARGE_CATEGORY);
            String mediumCategory = checkPcbKindExistForCategory(row, 2, PcbPartsSearchField.MEDIUM_CATEGORY);
            String smallCategory = checkPcbKindExistForCategory(row, 3, PcbPartsSearchField.SMALL_CATEGORY);
            String manufacturerName = checkPcbKindExistForCategory(targetPcbKindSearchMap, row, 6, PcbPartsSearchField.MANUFACTURER_NAME);
            String packaging = checkPcbKindExistForCategory(targetPcbKindSearchMap, row, 8, PcbPartsSearchField.PACKAGING);
            String offerName = checkPcbKindExistForCategory(targetPcbKindSearchMap, row, 17, PcbPartsSearchField.OFFER_NAME);

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
            pcbPartsSearch.setPrice1(this.excelSubService.getCellNumberValue(row, 10).intValue());
            pcbPartsSearch.setPrice2(this.excelSubService.getCellNumberValue(row, 11).intValue());
            pcbPartsSearch.setPrice3(this.excelSubService.getCellNumberValue(row, 12).intValue());
            pcbPartsSearch.setPrice4(this.excelSubService.getCellNumberValue(row, 13).intValue());
            pcbPartsSearch.setPrice5(this.excelSubService.getCellNumberValue(row, 14).intValue());
            pcbPartsSearch.setInventoryLevel(this.excelSubService.getCellNumberValue(row, 15).intValue());
            pcbPartsSearch.setMemo(this.excelSubService.getCellStrValue(row, 16));
            pcbPartsSearch.setOfferName(offerName);
            pcbPartsSearch.setMemberId(this.excelSubService.getCellStrValue(row, 18));
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

    @SuppressWarnings("DuplicatedCode")
    private void excelIndexingByOfferName(XSSFWorkbook workbook, int sheetAt, String offerName) {
        XSSFSheet sheet = workbook.getSheetAt(sheetAt); // 해당 엑셀파일의 시트(Sheet) 수
        int rows = sheet.getPhysicalNumberOfRows(); // 해당 시트의 행의 개수
        List<PcbPartsSearch> pcbPartsSearchList = new ArrayList<>();
        List<PcbKindSearch> pcbKindSearchList = new ArrayList<>();
        Map<String, PcbPartsSearch> pcbPartsSearchMap = new HashMap<>();
        Map<Integer, Map<String, PcbKindSearch>> targetPcbKindSearchMap = new HashMap<>();

        if (rows < 1) {
            log.info("pcb parts item indexing, data rows={}", rows);
            return;
        }

        for (int rowIndex = 1; rowIndex < rows; rowIndex++) {
            XSSFRow row = sheet.getRow(rowIndex); // 각 행을 읽어온다
            if (row == null) {
                continue;
            }

            // 특수문자 제거하여 저장
            String valueStr = this.excelSubService.getCellStrValue(row, PcbConstants.SimplePcbParts.PART_NUMBER.ordinal()).trim().replaceAll("[^a-zA-Z0-9]", "");

            PcbPartsSearch findPcbItem = pcbPartsSearchMap.get(valueStr);
            if (findPcbItem != null) {
                continue;
            }
            String manufacturerName = checkPcbKindExistForCategory(targetPcbKindSearchMap, row, PcbConstants.SimplePcbParts.MANUFACTURER_NAME.ordinal(), PcbPartsSearchField.MANUFACTURER_NAME);
            String packaging = checkPcbKindExistForCategory(targetPcbKindSearchMap, row, PcbConstants.SimplePcbParts.PACKAGE_DETAIL.ordinal(), PcbPartsSearchField.PACKAGING);

            PcbPartsSearch pcbPartsSearch = new PcbPartsSearch();
            pcbPartsSearch.setPartName(valueStr);
            pcbPartsSearch.setDescription(this.excelSubService.getCellStrValue(row, PcbConstants.SimplePcbParts.DESCRIPTION.ordinal()));
            pcbPartsSearch.setManufacturerName(manufacturerName);
            pcbPartsSearch.setPackaging(packaging);
            String qtyStr = this.excelSubService.getCellStrValue(row, PcbConstants.SimplePcbParts.INVENTORY_LEVEL.ordinal())
                    .replaceAll("[^a-zA-Z0-9가-힣]", ""); // 특수 문자 제거
            pcbPartsSearch.setInventoryLevel(Integer.parseInt(qtyStr));
            pcbPartsSearch.setOfferName(offerName);
            pcbPartsSearch.setStatus(PcbPartsSearchField.Status.APPROVED.ordinal());
            pcbPartsSearch.setDateCode(this.excelSubService.getCellStrValue(row, PcbConstants.SimplePcbParts.DATE_CODE.ordinal()));

            log.info("pcb parts item prepare indexing : parts name={}", valueStr);
            pcbPartsSearchList.add(pcbPartsSearch);
            pcbPartsSearchMap.put(valueStr, pcbPartsSearch);
        }


        targetPcbKindSearchMap.forEach((integer, stringPcbKindSearchMap) -> {
            stringPcbKindSearchMap.forEach((s, pcbKindSearch) -> {
                pcbKindSearchList.add(pcbKindSearch);
            });
        });

        if (pcbKindSearchList.size() > 0) {
            this.pcbKindSearchRepository.saveAll(pcbKindSearchList);
            log.info("pcb parts items, new kind items indexing");
        }

        this.pcbPartsSearchRepository.saveAll(pcbPartsSearchList);
        log.info("pcb parts items indexing");
    }

    /**
     * pcb kind 에 존재 하지는 검사 하여 없으면 넣지 않는다
     * @param row 로우
     * @param rowIdx 인덱스
     * @param targetName 대상명
     * @return 아이템명
     */
    private String checkPcbKindExistForCategory(XSSFRow row, int rowIdx, String targetName) {
        Integer target = PcbPartsSearchField.PCB_PART_TARGET_IDX_COLUMN.get(targetName);
        String value = this.excelSubService.getCellStrValue(row, rowIdx);
        if(StringUtils.isBlank(value)) {
            return "";
        }
        PcbKindSearch pcbKindSearch = pcbKindSearchRepository.findByItemNameKeywordAndTarget(value, target); // 아이템명으로 검색
        if(pcbKindSearch == null) {
            pcbKindSearch = pcbKindSearchRepository.findByDisplayNameKeywordAndTarget(value, target); // 디시플레이명으로 검색
            if(pcbKindSearch == null) {
                value = "";
            } else {
                value = pcbKindSearch.getItemName();
            }
        }
        if(!value.equals("")) {
            // 부모값 체크
            String pTargetName = "";
            if(targetName.equals(PcbPartsSearchField.MEDIUM_CATEGORY)) {
                pTargetName = PcbPartsSearchField.LARGE_CATEGORY;
            }
            if (targetName.equals(PcbPartsSearchField.SMALL_CATEGORY)) {
                pTargetName = PcbPartsSearchField.MEDIUM_CATEGORY;
            }
            if(!pTargetName.equals("")) { // 현재 검색된 kind 의 부모값과 엑셀의 부모값 일치 하는지 검사
                String parentValue = this.excelSubService.getCellStrValue(row, rowIdx - 1);
                if(StringUtils.isBlank(parentValue)) {
                    return "";
                }
                Optional<PcbKindSearch> parentPcbKindOpt = pcbKindSearchRepository.findById(pcbKindSearch.getpId());
                if(!parentPcbKindOpt.isPresent()) {
                    return "";
                }
                PcbKindSearch parentPcbKindSearch = parentPcbKindOpt.get();
                if(!parentValue.trim().equals(parentPcbKindSearch.getItemName()) && !parentValue.trim().equals(parentPcbKindSearch.getDisplayName())) {
                    return "";
                }
            }
        }
        return value;
    }

    /**
     * pcb kind 에 이미 존재 하는지 검사혀여 없으면 new kind list 넣어준다
     * @param targetPcbKindSearchMap 새로운 kind map(ref)
     * @param row 로우
     * @param rowIdx 인덱스
     * @param targetName 대상명
     * @return 로우 인덱스에서 가져온 데이터
     */
    private String checkPcbKindExistForCategory(Map<Integer, Map<String, PcbKindSearch>> targetPcbKindSearchMap, XSSFRow row, int rowIdx, String targetName) {
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

    private void excelIndexingByEleparts(XSSFWorkbook workbook, int sheetAt) {
        XSSFSheet sheet = workbook.getSheetAt(sheetAt); // 해당 엑셀파일의 시트(Sheet) 수
        int rows = sheet.getPhysicalNumberOfRows(); // 해당 시트의 행의 개수
        List<PcbPartsSearch> pcbPartsSearchList = new ArrayList<>();
        List<PcbKindSearch> pcbKindSearchList = new ArrayList<>();
        Map<String, PcbPartsSearch> pcbPartsSearchMap = new HashMap<>();
        Map<Integer, Map<String, PcbKindSearch>> targetPcbKindSearchMap = new HashMap<>();

        if (rows < 1) {
            log.info("pcb parts item indexing, data rows={}", rows);
            return;
        }

        for (int rowIndex = 1; rowIndex < rows; rowIndex++) {
            XSSFRow row = sheet.getRow(rowIndex); // 각 행을 읽어온다
            if (row == null) {
                continue;
            }

            // 특수문자 제거하여 저장
            String valueStr = this.excelSubService.getCellStrValue(row, 11).trim().replaceAll("[^a-zA-Z0-9]", "");

            PcbPartsSearch findPcbItem = pcbPartsSearchMap.get(valueStr);
            if (findPcbItem != null) {
                continue;
            }
//            String largeCategory = checkPcbKindExistForCategory(row, 1, PcbPartsSearchField.LARGE_CATEGORY);
//            String mediumCategory = checkPcbKindExistForCategory(row, 2, PcbPartsSearchField.MEDIUM_CATEGORY);
//            String smallCategory = checkPcbKindExistForCategory(row, 3, PcbPartsSearchField.SMALL_CATEGORY);
            String manufacturerName = checkPcbKindExistForCategory(targetPcbKindSearchMap, row, 12, PcbPartsSearchField.MANUFACTURER_NAME);
            String packaging = checkPcbKindExistForCategory(targetPcbKindSearchMap, row, 8, PcbPartsSearchField.PACKAGING);
//            String offerName = checkPcbKindExistForCategory(targetPcbKindSearchMap, row, 17, PcbPartsSearchField.OFFER_NAME);

            PcbPartsSearch pcbPartsSearch = new PcbPartsSearch();
//            pcbPartsSearch.setLargeCategory(largeCategory);
//            pcbPartsSearch.setMediumCategory(mediumCategory);
//            pcbPartsSearch.setSmallCategory(smallCategory);
            pcbPartsSearch.setPartName(this.excelSubService.getCellStrValue(row, 11));
            pcbPartsSearch.setDescription(this.excelSubService.getCellStrValue(row, 1));
            pcbPartsSearch.setManufacturerName(manufacturerName);
//            pcbPartsSearch.setPartsPackaging(this.excelSubService.getCellStrValue(row, 7));
            pcbPartsSearch.setPackaging(packaging);
//            pcbPartsSearch.setMoq(this.excelSubService.getCellNumberValue(row, 9).intValue());
            pcbPartsSearch.setPrice1(toPrice(row, 13));
            pcbPartsSearch.setPrice2(toPrice(row, 13));
            pcbPartsSearch.setPrice3(toPrice(row, 13));
            pcbPartsSearch.setPrice4(toPrice(row, 13));
            pcbPartsSearch.setPrice5(toPrice(row, 13));
//            pcbPartsSearch.setInventoryLevel(this.excelSubService.getCellNumberValue(row, 15).intValue());
//            pcbPartsSearch.setMemo(this.excelSubService.getCellStrValue(row, 16));
//            pcbPartsSearch.setOfferName(offerName);
//            pcbPartsSearch.setMemberId(this.excelSubService.getCellStrValue(row, 18));
            pcbPartsSearch.setStatus(PcbPartsSearchField.Status.APPROVED.ordinal());

            // watt
            pcbPartsSearch.setWatt(this.parsingToPcbUnitSearch(PcbPartsSearchField.WATT, this.excelSubService.getCellStrValue(row, 2)));
            // tolerance
            pcbPartsSearch.setTolerance(this.excelSubService.getCellStrValue(row, 3));
            // ohm
            pcbPartsSearch.setOhm(this.parsingToPcbUnitSearch(PcbPartsSearchField.OHM, this.excelSubService.getCellStrValue(row, 4)));
            // condenser
            pcbPartsSearch.setCondenser(this.parsingToPcbUnitSearch(PcbPartsSearchField.CONDENSER, this.excelSubService.getCellStrValue(row, 5)));
            // voltage
            pcbPartsSearch.setVoltage(this.parsingToPcbUnitSearch(PcbPartsSearchField.VOLTAGE, this.excelSubService.getCellStrValue(row, 6)));
            // temperature
            pcbPartsSearch.setTemperature(this.excelSubService.getCellStrValue(row, 7));
            // size
            pcbPartsSearch.setSize(this.excelSubService.getCellStrValue(row, 8));
            // current
            pcbPartsSearch.setCurrent(this.parsingToPcbUnitSearch(PcbPartsSearchField.CURRENT, this.excelSubService.getCellStrValue(row, 9)));
            // inductor
            pcbPartsSearch.setInductor(this.parsingToPcbUnitSearch(PcbPartsSearchField.INDUCTOR, this.excelSubService.getCellStrValue(row, 10)));

            log.info("pcb parts item prepare indexing : parts name={}", valueStr);
            pcbPartsSearchList.add(pcbPartsSearch);
            pcbPartsSearchMap.put(valueStr, pcbPartsSearch);
        }


        targetPcbKindSearchMap.forEach((integer, stringPcbKindSearchMap) -> {
            stringPcbKindSearchMap.forEach((s, pcbKindSearch) -> {
                pcbKindSearchList.add(pcbKindSearch);
            });
        });

        if (pcbKindSearchList.size() > 0) {
            this.pcbKindSearchRepository.saveAll(pcbKindSearchList);
            log.info("pcb parts items, new kind items indexing");
        }

        this.pcbPartsSearchRepository.saveAll(pcbPartsSearchList);
        log.info("pcb parts items indexing");
    }

    private PcbUnitSearch parsingToPcbUnitSearch(String propertyName, String value) {
        PcbUnitSearch pcbUnitSearch = new PcbUnitSearch();
        List<String> parsedSearchResults = PcbPartsUtils.parseString(value).get(propertyName);
        if (CollectionUtils.isNotEmpty(parsedSearchResults)) {
            String searchValue = parsedSearchResults.get(0);
            String lowerCaseString = searchValue.toLowerCase();
            // μF와 µF를 uF로 대체
            String replacedString = lowerCaseString
                    .replace("μF", "uF")
                    .replace("µF", "uF")
                    .replace("uf", "uF")
                    .replace("μV", "uV")
                    .replace("µV", "uV")
                    .replace("uv", "uV")
                    .replace("μA", "uA")
                    .replace("µA", "uA")
                    .replace("ua", "uA")
                    .replace("Ω", "Ohm");
            if (propertyName.equals(PcbPartsSearchField.CONDENSER)) {
                // 소문자로 변환
                PcbPartsUtils.FaradsConvert faradsConvert = new PcbPartsUtils.FaradsConvert();
                Map<PcbPartsUtils.FaradsConvert.Unit, String> faradsMap = faradsConvert.convert(replacedString);
                pcbUnitSearch.setField1(faradsMap.get(PcbPartsUtils.PcbConvert.Unit.FARADS));
                pcbUnitSearch.setField2(faradsMap.get(PcbPartsUtils.PcbConvert.Unit.MICROFARADS));
                pcbUnitSearch.setField3(faradsMap.get(PcbPartsUtils.PcbConvert.Unit.NANOFARADS));
                pcbUnitSearch.setField4(faradsMap.get(PcbPartsUtils.PcbConvert.Unit.PICOFARADS));
            }

            if (propertyName.equals(PcbPartsSearchField.TOLERANCE)) {
                PcbPartsUtils.ToleranceConvert toleranceConvert = new PcbPartsUtils.ToleranceConvert();
                Map<PcbPartsUtils.PcbConvert.Unit, String> tolerance = toleranceConvert.convert(replacedString);
                pcbUnitSearch.setField1(tolerance.get(PcbPartsUtils.PcbConvert.Unit.PERCENT));
                pcbUnitSearch.setField2(tolerance.get(PcbPartsUtils.PcbConvert.Unit.PERCENT_STRING));
            }

            if (propertyName.equals(PcbPartsSearchField.OHM)) {
                PcbPartsUtils.OhmConvert ohmConvert = new PcbPartsUtils.OhmConvert();
                Map<PcbPartsUtils.PcbConvert.Unit, String> tolerance = ohmConvert.convert(replacedString);
                pcbUnitSearch.setField1(tolerance.get(PcbPartsUtils.PcbConvert.Unit.OHMS));
                pcbUnitSearch.setField2(tolerance.get(PcbPartsUtils.PcbConvert.Unit.KILOHMS));
                pcbUnitSearch.setField3(tolerance.get(PcbPartsUtils.PcbConvert.Unit.MEGAOHMS));
            }

            if (propertyName.equals(PcbPartsSearchField.VOLTAGE)) {
                PcbPartsUtils.VoltConvert voltageConvert = new PcbPartsUtils.VoltConvert();
                Map<PcbPartsUtils.PcbConvert.Unit, String> voltage = voltageConvert.convert(replacedString);
                pcbUnitSearch.setField1(voltage.get(PcbPartsUtils.PcbConvert.Unit.VOLTS));
                pcbUnitSearch.setField2(voltage.get(PcbPartsUtils.PcbConvert.Unit.KILOVOLTS));
                pcbUnitSearch.setField3(voltage.get(PcbPartsUtils.PcbConvert.Unit.MILLIVOLTS));
                pcbUnitSearch.setField4(voltage.get(PcbPartsUtils.PcbConvert.Unit.MICROVOLTS));
            }

            if (propertyName.equals(PcbPartsSearchField.CURRENT)) {
                PcbPartsUtils.CurrentConvert currentConvert = new PcbPartsUtils.CurrentConvert();
                Map<PcbPartsUtils.PcbConvert.Unit, String> current = currentConvert.convert(replacedString);
                pcbUnitSearch.setField1(current.get(PcbPartsUtils.PcbConvert.Unit.AMPERES));
                pcbUnitSearch.setField2(current.get(PcbPartsUtils.PcbConvert.Unit.MILLIAMPERES));
                pcbUnitSearch.setField3(current.get(PcbPartsUtils.PcbConvert.Unit.MICROAMPERES));
            }

            if (propertyName.equals(PcbPartsSearchField.INDUCTOR)) {
                PcbPartsUtils.InductorConvert inductorConvert = new PcbPartsUtils.InductorConvert();
                Map<PcbPartsUtils.PcbConvert.Unit, String> inductor = inductorConvert.convert(replacedString);
                pcbUnitSearch.setField1(inductor.get(PcbPartsUtils.PcbConvert.Unit.HENRYS));
                pcbUnitSearch.setField2(inductor.get(PcbPartsUtils.PcbConvert.Unit.MILLIHENRYS));
                pcbUnitSearch.setField3(inductor.get(PcbPartsUtils.PcbConvert.Unit.MICROHENRYS));
            }
        }
        return pcbUnitSearch;
    }

    private int toPrice(XSSFRow row, int index) {
        String cellStrValue = this.excelSubService.getCellStrValue(row, index);
        // 숫자와 소수점을 제외한 모든 문자 제거
        String numericString = cellStrValue.replaceAll("[^\\d.]", "");
        if(StringUtils.isEmpty(numericString)) {
            return 0;
        }
        // 문자열을 double 형으로 변환
        // 반올림 수행
        return (int) Math.round(Double.parseDouble(numericString));
    }

    public void indexAllByEleparts(MultipartFile file) {
//        this.pcbPartsSearchRepository.deleteAll();
        XSSFWorkbook workbook = null;
        try {
            workbook = new XSSFWorkbook(file.getInputStream());
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                excelIndexingByEleparts(workbook, i);
            }
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
}
