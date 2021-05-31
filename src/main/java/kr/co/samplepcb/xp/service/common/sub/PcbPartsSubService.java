package kr.co.samplepcb.xp.service.common.sub;

import coolib.common.CCObjectResult;
import coolib.common.CCResult;
import kr.co.samplepcb.xp.domain.PcbPartsSearch;
import kr.co.samplepcb.xp.pojo.ElasticIndexName;
import kr.co.samplepcb.xp.pojo.PcbPartsSearchField;
import kr.co.samplepcb.xp.repository.PcbPartsSearchRepository;
import kr.co.samplepcb.xp.util.CoolElasticUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

@Service
public class PcbPartsSubService {

    private static final Logger log = LoggerFactory.getLogger(PcbPartsSubService.class);

    // search
    private final ElasticsearchRestTemplate elasticsearchRestTemplate;
    private final PcbPartsSearchRepository pcbPartsSearchRepository;

    public PcbPartsSubService(ElasticsearchRestTemplate elasticsearchRestTemplate, PcbPartsSearchRepository pcbPartsSearchRepository) {
        this.elasticsearchRestTemplate = elasticsearchRestTemplate;
        this.pcbPartsSearchRepository = pcbPartsSearchRepository;
    }

    /**
     * pcbParts 대상 아이템명 모두 변경
     * @param target 대상
     * @param from 이전값
     * @param to 갱신값
     * @return CCResult
     */
    public CCResult updateKindAllByGroup(int target, String from, String to) {

        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(matchQuery(PcbPartsSearchField.PCB_PART_COLUMN_IDX_TARGET[target] +".keyword", from))
                .build();
        SearchHits<PcbPartsSearch> searchResult = this.elasticsearchRestTemplate.search(searchQuery, PcbPartsSearch.class, IndexCoordinates.of(ElasticIndexName.PCB_PARTS));

        List<PcbPartsSearch> pcbPartsSearches = CoolElasticUtils.unwrapSearchHits(searchResult);
        if(pcbPartsSearches.size() > 0) {
            Field field = ReflectionUtils.findField(PcbPartsSearch.class, PcbPartsSearchField.PCB_PART_COLUMN_IDX_TARGET[target]);
            if (field == null) {
                CCResult ccResult = new CCResult();
                ccResult.setResult(false);
                ccResult.setMessage("find not field");
                return ccResult;
            }
            try {
                for (PcbPartsSearch pcbPartsSearch : pcbPartsSearches) {
                    field.setAccessible(true);
                    field.set(pcbPartsSearch, to);
                }
            } catch (IllegalAccessException e) {
                log.error(e.getMessage());
                return CCResult.exceptionSimpleMsg(e);
            }
            this.pcbPartsSearchRepository.saveAll(pcbPartsSearches);
        }
        return CCResult.ok();
    }

    public CCResult searchAllKeyword(String column, String query) {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(matchQuery(column +".keyword", query))
                .build();
        SearchHits<PcbPartsSearch> searchResult = this.elasticsearchRestTemplate.search(searchQuery, PcbPartsSearch.class, IndexCoordinates.of(ElasticIndexName.PCB_PARTS));
        return CCObjectResult.setSimpleData(CoolElasticUtils.unwrapSearchHits(searchResult));
    }
}
