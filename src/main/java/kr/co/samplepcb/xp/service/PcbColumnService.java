package kr.co.samplepcb.xp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import coolib.common.CCObjectResult;
import coolib.common.CCPagingResult;
import coolib.common.CCResult;
import coolib.common.QueryParam;
import coolib.util.CommonUtils;
import kr.co.samplepcb.xp.domain.PcbColumnSearch;
import kr.co.samplepcb.xp.pojo.*;
import kr.co.samplepcb.xp.pojo.adapter.PagingAdapter;
import kr.co.samplepcb.xp.repository.PcbColumnSearchRepository;
import kr.co.samplepcb.xp.service.common.sub.GoogleTensorService;
import kr.co.samplepcb.xp.util.CoolElasticUtils;
import kr.co.samplepcb.xp.util.CoolWebClientUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScriptScoreFunctionBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;

@Service
public class PcbColumnService {

    private static final Logger log = LoggerFactory.getLogger(PcbColumnService.class);

    // search
    private final RestHighLevelClient restHighLevelClient;
    private final PcbColumnSearchRepository pcbColumnSearchRepository;
    private final ElasticsearchRestTemplate elasticsearchRestTemplate;

    // service
    private final GoogleTensorService googleTensorService;

    public PcbColumnService(RestHighLevelClient restHighLevelClient, PcbColumnSearchRepository pcbColumnSearchRepository, ElasticsearchRestTemplate elasticsearchRestTemplate, GoogleTensorService googleTensorService) {
        this.restHighLevelClient = restHighLevelClient;
        this.pcbColumnSearchRepository = pcbColumnSearchRepository;
        this.elasticsearchRestTemplate = elasticsearchRestTemplate;
        this.googleTensorService = googleTensorService;
    }

    public void reindexAll() {

        this.pcbColumnSearchRepository.deleteAll();

        FileInputStream fis = null;
        XSSFWorkbook workbook = null;
        try {
            fis = new FileInputStream("column_dict.xlsx");
            workbook = new XSSFWorkbook(fis);
            XSSFSheet sheet = workbook.getSheetAt(0); // 해당 엑셀파일의 시트(Sheet) 수
            int rows = sheet.getPhysicalNumberOfRows(); // 해당 시트의 행의 개수
            for (int rowIndex = 1; rowIndex < rows; rowIndex++) {
                XSSFRow row = sheet.getRow(rowIndex); // 각 행을 읽어온다
                if (row == null) {
                    continue;
                }

                int cells = row.getPhysicalNumberOfCells();
                String list = "";
                int value = -1;
                for (int columnIndex = 0; columnIndex < cells; columnIndex++) {
                    XSSFCell cell = row.getCell(columnIndex); // 셀에 담겨있는 값을 읽는다.
                    if(cell == null) {
                        continue;
                    }
                    switch (cell.getCellType()) { // 각 셀에 담겨있는 데이터의 타입을 체크하고 해당 타입에 맞게 가져온다.
                        case NUMERIC:
                            value = (int) cell.getNumericCellValue();
                            break;
                        case STRING:
                            list = cell.getStringCellValue() + "";
                            break;
                        case BLANK:
                            list = cell.getBooleanCellValue() + "";
                            break;
                        case ERROR:
                            list = cell.getErrorCellValue() + "";
                            break;
                    }
                }
                PcbColumnSearch findPcbColumn = this.pcbColumnSearchRepository.findByColNameKeyword(list);
                if(findPcbColumn != null) {
                    continue;
                }
                PcbColumnSearch pcbColumnSearch = new PcbColumnSearch();
                pcbColumnSearch.setColName(list);
                pcbColumnSearch.setColNameKeyword(list);
                pcbColumnSearch.setTarget(value);

                CCObjectResult<List<Double>> vector = this.googleTensorService.requestDoc2vector(list);
                pcbColumnSearch.setColNameVector(vector.getData());

                this.pcbColumnSearchRepository.save(pcbColumnSearch);
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

    public CCResult search(Pageable pageable, QueryParam queryParam, PcbColumnSearchVM pcbColumnSearchVM) {
        BoolQueryBuilder query = boolQuery();
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        QueryBuilder queryBuilder;
        if(StringUtils.isEmpty(queryParam.getQ())) {
            queryBuilder = boolQuery();
        } else {
            queryBuilder = this.pcbColumnSearchRepository.makeWildcardPermitFieldQuery(queryParam.getQ(), query, highlightBuilder);
        }

        queryBuilder = this.pcbColumnSearchRepository.searchByColumnSearch(pcbColumnSearchVM, query, highlightBuilder);

        SearchResponse response = CoolElasticUtils.search(
                this.restHighLevelClient,
                queryBuilder,
                highlightBuilder,
                pageable,
                searchSourceBuilder -> {
                    searchSourceBuilder.trackTotalHits(true);
                },
                request -> request.indices(ElasticIndexName.PCB_COLUMN)
        );

        return PagingAdapter.toCCPagingResult(pageable, CoolElasticUtils.getSourceWithHighlightDetailInline(response), response.getHits().getTotalHits().value);
    }


    /**
     * google sentence vector 검색서버에 요청
     * @param pageable 페이징
     * @param contentsVector 백터
     * @return 결과
     */
    private CCPagingResult<PcbColumnSearch> requestSentence(Pageable pageable, List<Double> contentsVector) {
        Script script = new Script(Script.DEFAULT_SCRIPT_TYPE,
                "painless",
                "cosineSimilarity(params.query_vector, '" + PcbColumnSearchField.COL_NAME_VECTOR + "') + 1.0",
                Collections.singletonMap("query_vector", contentsVector)
        );

        BoolQueryBuilder query = boolQuery()
                .should(matchAllQuery())
                ;

        FunctionScoreQueryBuilder.FilterFunctionBuilder[] filterFunctions = {
                new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                        new ScriptScoreFunctionBuilder(script)
                )
        };
        Query searchQuery = new NativeSearchQueryBuilder()
                .withQuery(new FunctionScoreQueryBuilder(query, filterFunctions))
                .withSourceFilter(new FetchSourceFilter(null, new String[]{PcbColumnSearchField.COL_NAME_VECTOR}))
                .withPageable(pageable)
                .build();
        SearchHits<PcbColumnSearch> searchResult = this.elasticsearchRestTemplate.search(searchQuery, PcbColumnSearch.class, IndexCoordinates.of(ElasticIndexName.PCB_COLUMN));
        return PagingAdapter.toCCPagingResult(null, pageable, CoolElasticUtils.unwrapSearchHits(searchResult), searchResult.getTotalHits());
    }

    /**
     * sentence 검색과 점수반영
     * @param pageable 페이징
     * @param vector 백터
     * @param query 검색어
     * @return 결과
     */
    private CCPagingResult<PcbColumnSearch> searchSentenceScore(Pageable pageable, List<Double> vector, String query) {
        CCPagingResult<PcbColumnSearch> sentenceResult = this.requestSentence(pageable, vector);
        List<PcbColumnSearch> sentenceList = sentenceResult.getData();
        List<String> colNameList = sentenceList.stream().map(PcbColumnSearch::getColName).collect(Collectors.toList());
        // 점수 요청
        CCObjectResult<List<Double>> embedScoreResult = this.googleTensorService.requestEmbedScore(query, colNameList);
        List<Double> scoreList = embedScoreResult.getData();
        for (int i = 0; i < sentenceList.size(); i++) {
            Double score = scoreList.get(i);
            sentenceList.get(i).setGlScore(score * 100);
        }
        return sentenceResult;
    }


    /**
     * sentence 검색
     * @param pageable 페이징
     * @param queryParam 쿼리
     * @return 결과
     */
    public CCResult searchSentence(Pageable pageable, QueryParam queryParam) {
        // vector 요청
        List<Double> vector = this.googleTensorService.requestDoc2vector(queryParam.getQ()).getData();
        return searchSentenceScore(pageable, vector, queryParam.getQ());
    }

    /**
     * 한로우 단위로 sentence 검색
     * @param pageable 페이징
     * @param pcbSentenceVM 쿼리들
     * @return 결과
     */
    public CCResult searchSentenceList(Pageable pageable, PcbSentenceVM pcbSentenceVM) {

        // vector 요청
        List<String> columnNameList = pcbSentenceVM.getQueryColumnNameList();
        List<List<Double>> vectors = this.googleTensorService.requestDocs2vectors(columnNameList).getData();
        if(columnNameList.size() != vectors.size()) {
            log.warn("백터 변환이 제대로 안됨");
            return CCResult.ok();
        }

        // 검색된 정보
        List<Double> scoreList = new ArrayList<>();
        List<PcbColumnSearchVM> totalPcbColumnSearchList = new ArrayList<>();
        int emptyStrCnt = 0;
        for (int i = 0; i < vectors.size(); i++) {
            String columnName = columnNameList.get(i);
            if(StringUtils.isEmpty(columnName)) {
                PcbColumnSearchVM pcbVm = new PcbColumnSearchVM();
                pcbVm.setColumnIdx(i);
                totalPcbColumnSearchList.add(pcbVm);
                emptyStrCnt++;
                continue;
            }
            CCPagingResult<PcbColumnSearch> sentenceResult = this.searchSentenceScore(pageable, vectors.get(i), columnName);
            List<PcbColumnSearch> pcbColumnSearchList = sentenceResult.getData();
            if(pcbColumnSearchList.size() == 0) {
                continue;
            }
            PcbColumnSearch pcbColumnSearch = pcbColumnSearchList.get(0);
            PcbColumnSearchVM pcbColumnSearchVM = new PcbColumnSearchVM();
            BeanUtils.copyProperties(pcbColumnSearch, pcbColumnSearchVM);
            pcbColumnSearchVM.setQueryColName(columnName);
            pcbColumnSearchVM.setColumnIdx(i);
            totalPcbColumnSearchList.add(pcbColumnSearchVM);

            scoreList.add(pcbColumnSearch.getGlScore());
        }
        // 평균점수
        double averageScore = scoreList.stream().mapToDouble(v -> v).average().orElse(0);
        averageScore = averageScore - emptyStrCnt;

        pcbSentenceVM.setPcbColumnSearchList(totalPcbColumnSearchList);
        pcbSentenceVM.setAverageScore(averageScore);

        return CCObjectResult.setSimpleData(pcbSentenceVM);
    }


    @SuppressWarnings("rawtypes")
    public synchronized CCResult searchPartNumber(String partNumber) {
        String url = "https://api.mouser.com/api/v1/search/partnumber?apiKey=a6234c5d-764b-4867-8659-e6e15b419035";
        MouserParam mouserParam = new MouserParam();
        MouserParam.CSearchByPartRequest request = new MouserParam.CSearchByPartRequest();
        request.setMouserPartNumber(partNumber);
        request.setPartSearchOptions("1");
        mouserParam.setSearchByPartRequest(request);
        try {
            String mouserParamStr = CommonUtils.getObjectMapper().writeValueAsString(mouserParam);
            Map result = CoolWebClientUtils.requestPost(url, mouserParamStr);
            return CCObjectResult.setSimpleData(result);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            return CCResult.exceptionSimpleMsg(e);
        }
    }

    public CCResult indexing(PcbColumnSearchVM pcbColumnSearchVM) {
        PcbColumnSearch pcbColumnSearch = this.pcbColumnSearchRepository.findByColNameKeyword(pcbColumnSearchVM.getColName());

        if(pcbColumnSearch != null) {
            CCResult ccResult = new CCResult();
            ccResult.setResult(false);
            ccResult.setMessage("동일한 컬렴이 존재합니다.");
            return ccResult;
        }

        pcbColumnSearch = new PcbColumnSearch();
        BeanUtils.copyProperties(pcbColumnSearchVM, pcbColumnSearch);
        pcbColumnSearch.setColNameKeyword(pcbColumnSearchVM.getColName());

        CCObjectResult<List<Double>> vector = this.googleTensorService.requestDoc2vector(pcbColumnSearchVM.getColName());
        pcbColumnSearch.setColNameVector(vector.getData());

        this.pcbColumnSearchRepository.save(pcbColumnSearch);

        return CCObjectResult.setSimpleData(pcbColumnSearch);
    }

    public CCResult delete(String id) {
        this.pcbColumnSearchRepository.deleteById(id);
        return CCResult.ok();
    }
}
