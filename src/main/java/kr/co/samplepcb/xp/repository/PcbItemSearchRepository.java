package kr.co.samplepcb.xp.repository;

import coolib.common.QueryParam;
import kr.co.samplepcb.xp.domain.PcbItemSearch;
import kr.co.samplepcb.xp.pojo.PcbItemSearchField;
import kr.co.samplepcb.xp.pojo.PcbItemSearchVM;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface PcbItemSearchRepository extends ElasticsearchRepository<PcbItemSearch, String> {

    PcbItemSearch findByItemNameAndTarget(String itemName, int target);

    List<PcbItemSearch> findAllByTarget(int target);

    default QueryBuilder searchByColumnSearch(PcbItemSearchVM pcbItemSearchVM, QueryParam queryParam, BoolQueryBuilder refQuery, HighlightBuilder highlightBuilder) {
        if (pcbItemSearchVM.getTarget() != null) {
            refQuery.filter(QueryBuilders.matchQuery(PcbItemSearchField.TARGET, pcbItemSearchVM.getTarget()));
        }
        if (pcbItemSearchVM.getItemName() != null) {
            refQuery.filter(QueryBuilders.matchQuery(PcbItemSearchField.ITEM_NAME, pcbItemSearchVM.getItemName()));
        }
        if (StringUtils.isNotEmpty(queryParam.getQ())) {
            refQuery.must(QueryBuilders.matchQuery(PcbItemSearchField.ITEM_NAME_TEXT, queryParam.getQ()));
        }
        return refQuery;
    }

}
