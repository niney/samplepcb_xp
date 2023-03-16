package kr.co.samplepcb.xp.repository;

import kr.co.samplepcb.xp.domain.NotOctopartForSearch;
import kr.co.samplepcb.xp.domain.OctopartForSearch;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface NotOctopartSearchRepository extends ElasticsearchRepository<NotOctopartForSearch, String> {

    @Query("{\"match\": {\"mpn\": {\"query\": \"?0\"}}}")
    List<NotOctopartForSearch> findByMpn(String mpn);
}
