package kr.co.samplepcb.xp.util;

import coolib.util.CommonUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.functionscore.ExponentialDecayFunctionBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.springframework.data.elasticsearch.core.document.SearchDocumentResponse;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CoolElasticUtils {

    private static final Logger log = LoggerFactory.getLogger(CoolElasticUtils.class);
    public static final String highlightFields = "_highlightFields";


    public static List<Map<String, Object>> getSource(SearchResponse response) {
        return Arrays.stream(response.getHits().getHits()).map(SearchHit::getSourceAsMap).collect(Collectors.toList());
    }

    public static List<Map<String, Object>> getSourceWithHighlight(SearchResponse response) {
        List<Map<String, Object>> sourceList = new ArrayList<>();
        for (SearchHit hit : response.getHits().getHits()) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            List<String> hfList = new ArrayList<>();
            hit.getHighlightFields().forEach((field, highlightField) -> {
                hfList.add(field);
            });
            if (hfList.size() > 0) {
                sourceAsMap.put(highlightFields, hfList);
            }
            sourceList.add(sourceAsMap);
        }
        return sourceList;
    }

    public static List<Map<String, Object>> getSourceWithHighlightDetail(SearchResponse response) {
        List<Map<String, Object>> sourceList = new ArrayList<>();
        for (SearchHit hit : response.getHits().getHits()) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            Map<String, Object> hfList = new HashMap<>();
            hit.getHighlightFields().forEach((field, highlightField) -> {
                List<String> fragmentList = new ArrayList<>();
                for (Text fragment : highlightField.getFragments()) {
                    fragmentList.add(fragment.toString());
                }
                hfList.put(field, fragmentList);
            });
            if (hfList.size() > 0) {
                sourceAsMap.put(highlightFields, hfList);
            }
            sourceList.add(sourceAsMap);
        }
        return sourceList;
    }

    public static List<Map<String, Object>> getSourceWithHighlightDetailInline(SearchResponse response) {
        List<Map<String, Object>> sourceList = new ArrayList<>();
        for (SearchHit hit : response.getHits().getHits()) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            sourceAsMap.put("id", hit.getId());
            Map<String, Object> hfList = new HashMap<>();
            hit.getHighlightFields().forEach((field, highlightField) -> {
                String fragments = Arrays.stream(highlightField.getFragments()).map(s -> String.format("%s ...", s)).collect(Collectors.joining());
                hfList.put(field, fragments);
            });
            if (hfList.size() > 0) {
                sourceAsMap.put(highlightFields, hfList);
            }
            sourceList.add(sourceAsMap);
        }
        return sourceList;
    }

    public static List<Map<String, Object>> getSourceWithHighlightDetailInline(SearchResponse response, Consumer<Map<String, Object>> callback) {
        List<Map<String, Object>> sourceList = new ArrayList<>();
        for (SearchHit hit : response.getHits().getHits()) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            callback.accept(sourceAsMap);
            Map<String, Object> hfList = new HashMap<>();
            hit.getHighlightFields().forEach((field, highlightField) -> {
                String fragments = Arrays.stream(highlightField.getFragments()).map(s -> String.format("%s ...", s)).collect(Collectors.joining());
                hfList.put(field, fragments);
            });
            if (hfList.size() > 0) {
                sourceAsMap.put(highlightFields, hfList);
            }
            sourceList.add(sourceAsMap);
        }
        return sourceList;
    }

    public static List<Map<String, Object>> getSourceWithHighlightOriginDetail(SearchResponse response, boolean replace, String preTags, String postTags) {
        List<Map<String, Object>> sourceList = new ArrayList<>();
        for (SearchHit hit : response.getHits().getHits()) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            Map<String, Object> hfList = new HashMap<>();
            hit.getHighlightFields().forEach((field, highlightField) -> {
                String[] dotSplit = field.split("\\.");
                if(dotSplit.length > 1) {
                    field = dotSplit[0];
                }
                if(!(sourceAsMap.get(field) instanceof String)) {
                    return;
                }
                String fieldValue = (String) sourceAsMap.get(field);
                for (Text fragment : highlightField.getFragments()) {
                    String removeTagFrag = fragment.toString()
                            .replace(preTags, "")
                            .replace(postTags, "");
                    fieldValue = fieldValue.replace(removeTagFrag, fragment.toString());
                }
                hfList.put(field, fieldValue);
            });
            if (hfList.size() > 0) {
                if(replace) {
                    hfList.forEach(sourceAsMap::put);
                } else {
                    sourceAsMap.put(highlightFields, hfList);
                }

            }
            sourceList.add(sourceAsMap);
        }
        return sourceList;
    }

    public static List<Map<String, Object>> getSourceWithHighlightOriginDetail(SearchResponse response, boolean replace, String tag) {
        return CoolElasticUtils.getSourceWithHighlightOriginDetail(response, replace, "<" + tag + ">", "</" + tag + ">");
    }

    public static List<Map<String, Object>> getSourceWithHighlightOriginDetail(SearchResponse response, boolean replace) {
        return CoolElasticUtils.getSourceWithHighlightOriginDetail(response, replace, "<em>", "</em>");
    }

    public static long getCount(RestHighLevelClient highLevelClient, QueryBuilder queryBuilder, String... indices) {
        SearchResponse response = CoolElasticUtils.search(highLevelClient, queryBuilder, Pageable.unpaged(), null, request -> {
            request.indices(indices);
        });
         return response.getHits().getTotalHits().value;
    }

    public static void addSort(SearchRequestBuilder refBuilder, Sort.Order order) {
        refBuilder.addSort(order.getProperty(),
                order.getDirection().isAscending() ? SortOrder.ASC : SortOrder.DESC);
    }

    public static void addSort(SearchSourceBuilder refBuilder, Sort.Order order) {
        refBuilder.sort(order.getProperty(),
                order.getDirection().isAscending() ? SortOrder.ASC : SortOrder.DESC);
    }

    public static SearchSourceBuilder createBuildSearchRequest(QueryBuilder queryBuilder, Pageable pageable) {
        if(pageable == null) {
            return new SearchSourceBuilder()
                    .query(queryBuilder);
        }
        if(pageable.isUnpaged()) {
            return new SearchSourceBuilder()
                    .query(queryBuilder).size(0);
        }
        return new SearchSourceBuilder()
                .query(queryBuilder)
                .from(pageable.getPageNumber() * pageable.getPageSize())
                .size(pageable.getPageSize());
    }

    public static SearchRequest createSearchResponse(SearchSourceBuilder builder, Pageable pageable) {
        SearchRequest searchRequest = new SearchRequest();
        if(pageable != null && pageable.isPaged()) {
            for (Sort.Order order : pageable.getSort()) {
                CoolElasticUtils.addSort(builder, order);
            }
        }
        searchRequest.source(builder);

        return searchRequest;
    }

    public static SearchResponse search(RestHighLevelClient highLevelClient, SearchRequest searchRequest) {
        SearchResponse response;
        try {
            response = highLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error(CommonUtils.getFullStackTrace(e));
            throw new RuntimeException(e);
        }
        return response;
    }

    /**
     * rest high level client 검색
     * @param queryBuilder 쿼리빌더
     * @param highlightBuilder 하이라이트
     * @param pageable 페이징
     * @param sourceBuilderFn 소스빌더 콜백
     * @param requestFn 요청객체 콜백
     * @return response 결과
     */
    public static SearchResponse search(RestHighLevelClient client, QueryBuilder queryBuilder, HighlightBuilder highlightBuilder, Pageable pageable,
                                 Consumer<SearchSourceBuilder> sourceBuilderFn,
                                 Consumer<SearchRequest> requestFn) {
        SearchSourceBuilder builder = CoolElasticUtils.createBuildSearchRequest(queryBuilder, pageable);
        if(sourceBuilderFn != null) {
            sourceBuilderFn.accept(builder);
        }
        if(highlightBuilder != null) {
            builder.highlighter(highlightBuilder);
        }
        SearchRequest request = CoolElasticUtils.createSearchResponse(builder, pageable);
        if(requestFn != null) {
            requestFn.accept(request);
        }
        return CoolElasticUtils.search(client, request);
    }

    /**
     * rest high level client 검색, 하이라이트 없이
     * @param queryBuilder 쿼리빌더
     * @param pageable 페이징
     * @param sourceBuilderFn 소스빌더 콜백
     * @param requestFn 요청객체 콜백
     * @return response 결과
     */
    public static SearchResponse search(RestHighLevelClient client, QueryBuilder queryBuilder, Pageable pageable,
                                 Consumer<SearchSourceBuilder> sourceBuilderFn,
                                 Consumer<SearchRequest> requestFn) {
        return CoolElasticUtils.search(client, queryBuilder, null, pageable, sourceBuilderFn, requestFn);
    }

    public static SearchResponse simpleSearchResponse(RestHighLevelClient highLevelClient, String indexName, Pageable pageable, SearchSourceBuilder builder) {
        SearchRequest searchRequest = new SearchRequest();
        for (Sort.Order order : pageable.getSort()) {
            CoolElasticUtils.addSort(builder, order);
        }

        searchRequest.source(builder)
                .indices(indexName);

        SearchResponse response = null;
        try {
            response = highLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error(CommonUtils.getFullStackTrace(e));
        }
        return response;
    }

    public static ScoreFunctionBuilder<?> defaultGaussDecayFnBuilder(String fieldName) {
        // level1
//        return new ExponentialDecayFunctionBuilder(fieldName, new Date().getTime(), "1000000000000d", "1000d", 0.1);
//         level2
//        new ExponentialDecayFunctionBuilder()
        return new ExponentialDecayFunctionBuilder(fieldName, new Date().getTime(), "100000000000d", "1000d", 0.1);
        // level3
//        return new ExponentialDecayFunctionBuilder(fieldName, new Date().getTime(), "10000000000d", "1000d", 0.1);
    }

    public static HighlightBuilder simpleCreateHighlight(String... names) {
        return CoolElasticUtils.simpleCreateHighlightWithTag(null, names);
    }

    public static HighlightBuilder simpleCreateHighlightWithTag(String tag, String... names) {
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        for (String name : names) {
            if(tag == null ) {
                highlightBuilder.field(name);
            } else {
                highlightBuilder.field(name).preTags("<" + tag + ">").postTags("</" + tag + ">");
            }
        }
        return highlightBuilder;
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> unwrapSearchHits(SearchHits<T> searchHits) {
        return (List<T>) SearchHitSupport.unwrapSearchHits(searchHits);
    }

    public static <T> List<T> toEntities(Class<T> cls, SearchDocumentResponse documentResponse, ElasticsearchConverter elasticsearchConverter) {
        return documentResponse.getSearchDocuments()
                .stream()
                .map(searchDocument -> elasticsearchConverter.read(cls, searchDocument))
                .collect(Collectors.toList());
    }
}
