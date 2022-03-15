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

    public PcbPartsService(RestHighLevelClient restHighLevelClient, PcbPartsSearchRepository pcbPartsSearchRepository, PcbKindSearchRepository pcbKindSearchRepository, ExcelSubService excelSubService, ApplicationProperties applicationProperties) {
        this.restHighLevelClient = restHighLevelClient;
        this.pcbPartsSearchRepository = pcbPartsSearchRepository;
        this.pcbKindSearchRepository = pcbKindSearchRepository;
        this.excelSubService = excelSubService;
        this.applicationProperties = applicationProperties;
        this.samplepcbToken = applicationProperties.getAuth().getSamplepcbSiteToken();
    }

    public CCResult indexing(PcbPartsSearchVM pcbPartsSearchVM) {

        PcbPartsSearch pcbPartsSearch = new PcbPartsSearch();
        if(StringUtils.isNotEmpty(pcbPartsSearchVM.getId())) {
            Optional<PcbPartsSearch> findPcbPartsOpt = this.pcbPartsSearchRepository.findById(pcbPartsSearchVM.getId());
            if(!findPcbPartsOpt.isPresent()) {
                return CCResult.dataNotFound();
            }
            pcbPartsSearch = findPcbPartsOpt.get();
        } else {
            PcbPartsSearch findPcbParts = this.pcbPartsSearchRepository.findByPartNameNormalizeAndMemberId(pcbPartsSearchVM.getPartName(), pcbPartsSearchVM.getMemberId());
            if(findPcbParts != null) {
                CCResult ccResult = new CCResult();
                ccResult.setResult(false);
                ccResult.setMessage("동일한 상품을 등록할 수 없습니다.");
                return ccResult;
            }
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

    @SuppressWarnings({"rawtypes"})
    public CCResult search(Pageable pageable, QueryParam queryParam, PcbPartsSearchVM pcbPartsSearchVM) {
        BoolQueryBuilder query = boolQuery();
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        BoolQueryBuilder queryBuilder;
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
                queryBuilder.filter(statusQuery);
            }
        } else if (StringUtils.isEmpty(pcbPartsSearchVM.getMemberId())) { // member id 가 비어있으면 filter 걸기
            queryBuilder.filter(QueryBuilders.matchQuery(PcbPartsSearchField.STATUS, 1));
        }

        SearchResponse response = CoolElasticUtils.search(
                this.restHighLevelClient,
                new FunctionScoreQueryBuilder(queryBuilder, CoolElasticUtils.defaultGaussDecayFnBuilder(PcbPartsSearchField.WRITE_DATE)),
                highlightBuilder,
                pageable,
                null,
                request -> request.indices(ElasticIndexName.PCB_PARTS)
        );
        List<Map<String, Object>> respData = CoolElasticUtils.getSourceWithHighlightDetailInline(response);

        if(!StringUtils.isEmpty(pcbPartsSearchVM.getId()) && respData.size() == 1) {
            Map<String, Object> pcbParts = respData.get(0);
            String memberId = (String) pcbParts.get(PcbPartsSearchField.MEMBER_ID);
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
                    .bodyToMono(new ParameterizedTypeReference<CCObjectResult<Map>>() {}).block();
            if(memberResult != null && memberResult.getData() != null) {
                Map member = memberResult.getData();
                String memberName = (String) member.get("mb_name");
                String memberTel = (String) member.get("mb_tel");
                String memberEmail = (String) member.get("mb_email");
                pcbParts.put(PcbPartsSearchField.MANAGER_NAME, memberName);
                pcbParts.put(PcbPartsSearchField.MANAGER_PHONE_NUMBER, memberTel);
                pcbParts.put(PcbPartsSearchField.MANAGER_EMAIL, memberEmail);
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
}
