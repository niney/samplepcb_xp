package kr.co.samplepcb.xp.repository;

import kr.co.samplepcb.xp.domain.OctopartForSearch;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface OctopartSearchRepository extends ElasticsearchRepository<OctopartForSearch, String> {

    @Query("{\"match\": {\"mpn\": {\"query\": \"?0\"}}}")
    List<OctopartForSearch> findByMpn(String mpn);
}
