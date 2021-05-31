package kr.co.samplepcb.xp.repository;

import coolib.common.QueryParam;
import kr.co.samplepcb.xp.domain.PcbKindSearch;
import kr.co.samplepcb.xp.pojo.PcbKindSearchField;
import kr.co.samplepcb.xp.pojo.PcbKindSearchVM;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface PcbKindSearchRepository extends ElasticsearchRepository<PcbKindSearch, String> {

    PcbKindSearch findByItemNameAndTarget(String itemName, int target);

    @Query("{\n" +
            "    \"bool\": {\n" +
            "      \"must\": [\n" +
            "        {\n" +
            "          \"match\": {\n" +
            "            \"itemName.keyword\": \"?0\"\n" +
            "          }\n" +
            "        },\n" +
            "        {\n" +
            "          \"match\": {\n" +
            "            \"target\": ?1\n" +
            "          }\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  }")
    PcbKindSearch findByItemNameKeywordAndTarget(String itemName, int target);

    List<PcbKindSearch> findAllByTarget(int target);

    default QueryBuilder searchByItemSearch(PcbKindSearchVM pcbKindSearchVM, QueryParam queryParam, BoolQueryBuilder refQuery, HighlightBuilder highlightBuilder) {
        if (pcbKindSearchVM.getTarget() != null) {
            refQuery.filter(QueryBuilders.matchQuery(PcbKindSearchField.TARGET, pcbKindSearchVM.getTarget()));
        }
        if (pcbKindSearchVM.getItemName() != null) {
            refQuery.filter(QueryBuilders.matchQuery(PcbKindSearchField.ITEM_NAME, pcbKindSearchVM.getItemName()));
        }
        if (StringUtils.isNotEmpty(queryParam.getQ())) {
            refQuery.must(QueryBuilders.matchQuery(PcbKindSearchField.ITEM_NAME_TEXT, queryParam.getQ()));
        }
        return refQuery;
    }

}
