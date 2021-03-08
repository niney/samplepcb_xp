package kr.co.samplepcb.xp.repository;

import kr.co.samplepcb.xp.domain.PcbColumnSearch;
import kr.co.samplepcb.xp.pojo.PcbColumnSearchField;
import kr.co.samplepcb.xp.pojo.PcbColumnSearchVM;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
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

    PcbColumnSearch findByColNameKeyword(String colName);

    default QueryBuilder searchByColumnSearch(PcbColumnSearchVM pcbColumnSearchVM, BoolQueryBuilder refQuery, HighlightBuilder highlightBuilder) {
        if (pcbColumnSearchVM.getTarget() != null) {
            refQuery.filter(QueryBuilders.matchQuery(PcbColumnSearchField.TARGET, pcbColumnSearchVM.getTarget()));
        }
        if (pcbColumnSearchVM.getColName() != null) {
            refQuery.filter(QueryBuilders.matchQuery(PcbColumnSearchField.COL_NAME, pcbColumnSearchVM.getColName()));
        }
        return refQuery;
    }
}
