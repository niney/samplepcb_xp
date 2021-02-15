package kr.co.samplepcb.xp.repository;

import kr.co.samplepcb.xp.domain.PcbColumnSearch;
import kr.co.samplepcb.xp.pojo.PcbColumnSearchField;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

public interface PcbColumnSearchRepository extends ElasticsearchRepository<PcbColumnSearch, String> {

    default QueryBuilder makeWildcardPermitFieldQuery(String query, BoolQueryBuilder refQuery) {
        return refQuery
                .must(QueryBuilders.matchQuery(PcbColumnSearchField.COL_NAME, query));
    }

    default QueryBuilder makeWildcardPermitFieldQuery(String query, BoolQueryBuilder refQuery, HighlightBuilder highlightBuilder) {
        highlightBuilder.field(new HighlightBuilder.Field(PcbColumnSearchField.COL_NAME));
        return this.makeWildcardPermitFieldQuery(query, refQuery);
    }

    PcbColumnSearch findByColName(String colName);
}
