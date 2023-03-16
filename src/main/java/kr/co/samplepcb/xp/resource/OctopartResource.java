package kr.co.samplepcb.xp.resource;

import coolib.common.CCResult;
import kr.co.samplepcb.xp.domain.NotOctopartForSearch;
import kr.co.samplepcb.xp.domain.OctopartForSearch;
import kr.co.samplepcb.xp.service.common.sub.MlOctopartSubService;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/octopart")
public class OctopartResource {

    // search
    private final ElasticsearchOperations elasticsearchOperations;
    // service
    private final MlOctopartSubService mlOctopartSubService;

    public OctopartResource(ElasticsearchOperations elasticsearchOperations, MlOctopartSubService mlOctopartSubService) {
        this.elasticsearchOperations = elasticsearchOperations;
        this.mlOctopartSubService = mlOctopartSubService;
    }

    @GetMapping("/_createIndex")
    public CCResult reindexAll() {
        IndexOperations indexOperations = this.elasticsearchOperations.indexOps(OctopartForSearch.class);
        Document document = indexOperations.createMapping();
        indexOperations.putMapping(document);

        indexOperations = this.elasticsearchOperations.indexOps(NotOctopartForSearch.class);
        document = indexOperations.createMapping();
        indexOperations.putMapping(document);
        return CCResult.ok();
    }

    @PostMapping(value = "/_indexing", produces = {"application/json", "application/x-www-form-urlencoded"})
    public CCResult indexing(@RequestBody OctopartForSearch octopartForSearch) {
        return this.mlOctopartSubService.indexing(octopartForSearch);
    }
}
