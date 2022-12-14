package kr.co.samplepcb.xp.repository;

import coolib.common.QueryParam;
import kr.co.samplepcb.xp.domain.PcbPartsSearch;
import kr.co.samplepcb.xp.pojo.PcbPartsSearchField;
import kr.co.samplepcb.xp.pojo.PcbPartsSearchVM;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.lang.reflect.Field;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.*;

public interface PcbPartsSearchRepository extends ElasticsearchRepository<PcbPartsSearch, String> {

    Logger log = LoggerFactory.getLogger(PcbPartsSearchRepository.class);

    @Query("{\n" +
            "    \"bool\": {\n" +
            "      \"must\": [\n" +
            "        {\n" +
            "          \"match\": {\n" +
            "            \"partName.normalize\": \"?0\"\n" +
            "          }\n" +
            "        },\n" +
            "        {\n" +
            "          \"match\": {\n" +
            "            \"memberId\": \"?1\"\n" +
            "          }\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  }")
    PcbPartsSearch findByPartNameNormalizeAndMemberId(String partName, String memberId);


    default QueryBuilder makeWildcardPermitFieldQuery(String qf, String query, BoolQueryBuilder refQuery) {
        if (StringUtils.isNotEmpty(qf)) {
            return refQuery.must(matchQuery(qf, query));
        }
        return refQuery
                .must(queryStringQuery(QueryParser.escape(query))
                        .field(PcbPartsSearchField.PART_NAME)
                        .field(PcbPartsSearchField.MANUFACTURER_NAME)
                        .field(PcbPartsSearchField.LARGE_CATEGORY)
                        .field(PcbPartsSearchField.MEDIUM_CATEGORY)
                        .field(PcbPartsSearchField.SMALL_CATEGORY)
                );
    }

    default QueryBuilder makeWildcardPermitFieldQuery(String qf, String query, BoolQueryBuilder refQuery, HighlightBuilder highlightBuilder) {
        highlightBuilder.field(new HighlightBuilder.Field(PcbPartsSearchField.PART_NAME));
        highlightBuilder.field(new HighlightBuilder.Field(PcbPartsSearchField.MANUFACTURER_NAME));
        highlightBuilder.field(new HighlightBuilder.Field(PcbPartsSearchField.LARGE_CATEGORY));
        highlightBuilder.field(new HighlightBuilder.Field(PcbPartsSearchField.MEDIUM_CATEGORY));
        highlightBuilder.field(new HighlightBuilder.Field(PcbPartsSearchField.SMALL_CATEGORY));
        return this.makeWildcardPermitFieldQuery(qf, query, refQuery);
    }

    default BoolQueryBuilder searchByColumnSearch(PcbPartsSearchVM pcbPartsSearchVM, QueryParam queryParam, BoolQueryBuilder refQuery, HighlightBuilder highlightBuilder) {
        BoolQueryBuilder categoryQuery = QueryBuilders.boolQuery();
        for (Field pcbPartsSearchField : PcbPartsSearchVM.pcbPartsSearchFields) {
            try {
                Object value = pcbPartsSearchField.get(pcbPartsSearchVM);
                if(value instanceof String && StringUtils.isNotEmpty((CharSequence) value)) {
                    String name = pcbPartsSearchField.getName();
                    if(name.equals("token")) {
                        continue;
                    }
                    // 카테고리 대중소 or 로 만들어줌
                    if(name.equals(PcbPartsSearchField.LARGE_CATEGORY) ||
                            name.equals(PcbPartsSearchField.MEDIUM_CATEGORY) ||
                            name.equals(PcbPartsSearchField.SMALL_CATEGORY) ||
                            name.equals(PcbPartsSearchField.SERVICE_TYPE)) {
                        categoryQuery.should(QueryBuilders.matchQuery(name, value));
                        continue;
                    }
                    if(name.equals("id")) {
                        name = "_id";
                        refQuery.filter(QueryBuilders.matchQuery(name, value));
                    } else {
                        HighlightBuilder.Field field = new HighlightBuilder.Field(name);
                        if(!highlightBuilder.fields().contains(field)) {
                            highlightBuilder.field(field);
                        }
                        refQuery.must(QueryBuilders.matchQuery(name, value));
                    }
                }
            } catch (IllegalAccessException e) {
                log.error(e.getMessage());
            }
        }
        if(CollectionUtils.isNotEmpty(pcbPartsSearchVM.getIds())) {
            BoolQueryBuilder idsQuery = QueryBuilders.boolQuery();
            List<String> ids = pcbPartsSearchVM.getIds();
            for (String id : ids) {
                idsQuery.should(matchQuery("_id", id));
            }
            refQuery.must(idsQuery);
        }
        // 카테고리 쿼리가 존재하면 적용
        if(categoryQuery.hasClauses()) {
            refQuery.filter(categoryQuery);
        }
        // serviceType 기본은 필드 존재 하지 않음
        if (StringUtils.isEmpty(pcbPartsSearchVM.getServiceType())) {
            refQuery.mustNot(existsQuery(PcbPartsSearchField.SERVICE_TYPE));
        } else {
            refQuery.must(matchQuery(PcbPartsSearchField.SERVICE_TYPE, pcbPartsSearchVM.getServiceType()));
        }
        return refQuery;
    }
}
