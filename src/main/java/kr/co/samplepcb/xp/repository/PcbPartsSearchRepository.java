package kr.co.samplepcb.xp.repository;

import coolib.common.QueryParam;
import kr.co.samplepcb.xp.domain.PcbPartsSearch;
import kr.co.samplepcb.xp.pojo.PcbPartsSearchField;
import kr.co.samplepcb.xp.pojo.PcbPartsSearchVM;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.lang.reflect.Field;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

public interface PcbPartsSearchRepository extends ElasticsearchRepository<PcbPartsSearch, String> {

    Logger log = LoggerFactory.getLogger(PcbPartsSearchRepository.class);


    default QueryBuilder makeWildcardPermitFieldQuery(String query, BoolQueryBuilder refQuery) {
        return refQuery
                .must(queryStringQuery(QueryParser.escape(query))
                        .field(PcbPartsSearchField.PART_NAME)
                        .field(PcbPartsSearchField.MANUFACTURER_NAME)
                        .field(PcbPartsSearchField.LARGE_CATEGORY)
                        .field(PcbPartsSearchField.MEDIUM_CATEGORY)
                        .field(PcbPartsSearchField.SMALL_CATEGORY)
                        .field(PcbPartsSearchField.DESCRIPTION)
                );
    }

    default QueryBuilder makeWildcardPermitFieldQuery(String query, BoolQueryBuilder refQuery, HighlightBuilder highlightBuilder) {
        highlightBuilder.field(new HighlightBuilder.Field(PcbPartsSearchField.PART_NAME));
        highlightBuilder.field(new HighlightBuilder.Field(PcbPartsSearchField.MANUFACTURER_NAME));
        highlightBuilder.field(new HighlightBuilder.Field(PcbPartsSearchField.LARGE_CATEGORY));
        highlightBuilder.field(new HighlightBuilder.Field(PcbPartsSearchField.MEDIUM_CATEGORY));
        highlightBuilder.field(new HighlightBuilder.Field(PcbPartsSearchField.SMALL_CATEGORY));
        highlightBuilder.field(new HighlightBuilder.Field(PcbPartsSearchField.DESCRIPTION));
        return this.makeWildcardPermitFieldQuery(query, refQuery);
    }

    default QueryBuilder searchByColumnSearch(PcbPartsSearchVM pcbPartsSearchVM, QueryParam queryParam, BoolQueryBuilder refQuery, HighlightBuilder highlightBuilder) {
        for (Field pcbPartsSearchField : PcbPartsSearchVM.pcbPartsSearchFields) {
            try {
                Object value = pcbPartsSearchField.get(pcbPartsSearchVM);
                if(value instanceof String && StringUtils.isNotEmpty((CharSequence) value)) {
                    String name = pcbPartsSearchField.getName();
                    if(name.equals("token")) {
                        continue;
                    }
                    if(name.equals("id")) {
                        name = "_id";
                        refQuery.filter(QueryBuilders.matchQuery(name, value));
                    } else {
                        highlightBuilder.field(new HighlightBuilder.Field(name));
                        refQuery.must(QueryBuilders.matchQuery(name, value));
                    }
                }
            } catch (IllegalAccessException e) {
                log.error(e.getMessage());
            }
        }
        return refQuery;
    }
}
