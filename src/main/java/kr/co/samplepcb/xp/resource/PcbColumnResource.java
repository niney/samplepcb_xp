package kr.co.samplepcb.xp.resource;

import coolib.common.CCResult;
import coolib.common.QueryParam;
import kr.co.samplepcb.xp.domain.PcbColumnSearch;
import kr.co.samplepcb.xp.pojo.PcbColumnSearchVM;
import kr.co.samplepcb.xp.pojo.PcbSentenceVM;
import kr.co.samplepcb.xp.repository.PcbColumnSearchRepository;
import kr.co.samplepcb.xp.service.PcbColumnService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pcbColumn")
public class PcbColumnResource {

    // search
    private final ElasticsearchOperations elasticsearchOperations;
    private final PcbColumnSearchRepository pcbColumnSearchRepository;

    // service
    private final PcbColumnService pcbColumnService;

    public PcbColumnResource(PcbColumnSearchRepository pcbColumnSearchRepository, ElasticsearchOperations elasticsearchOperations, PcbColumnService pcbColumnService) {
        this.pcbColumnSearchRepository = pcbColumnSearchRepository;
        this.elasticsearchOperations = elasticsearchOperations;
        this.pcbColumnService = pcbColumnService;
    }

    @GetMapping("/_reindexAll")
    public CCResult reindexAll() {
        IndexOperations indexOperations = this.elasticsearchOperations.indexOps(PcbColumnSearch.class);
        Document document = indexOperations.createMapping();
        indexOperations.putMapping(document);
        this.pcbColumnService.reindexAll();

        return CCResult.ok();
    }

    @GetMapping("/_search")
    public CCResult search(@PageableDefault Pageable pageable, QueryParam queryParam, PcbColumnSearchVM pcbColumnSearchVM) {
        return this.pcbColumnService.search(pageable, queryParam, pcbColumnSearchVM);
    }

    @GetMapping("/_searchSentence")
    public CCResult searchSentence(@PageableDefault(size = 3) Pageable pageable, QueryParam queryParam) {
        return this.pcbColumnService.searchSentence(pageable, queryParam);
    }

    @PostMapping("/_searchSentenceList")
    public CCResult searchSentenceList(@PageableDefault(size = 3) Pageable pageable, @RequestBody PcbSentenceVM pcbSentenceVM) {
        return this.pcbColumnService.searchSentenceList(pageable, pcbSentenceVM);
    }

    @GetMapping("/_searchPartNumber")
    public CCResult reindexAllManufacturer(String partNumber) throws InterruptedException {
        return this.pcbColumnService.searchPartNumber(partNumber);
    }

    @PostMapping("/_indexing")
    public CCResult indexing(PcbColumnSearchVM pcbColumnSearchVM) {
        return this.pcbColumnService.indexing(pcbColumnSearchVM);
    }

    @GetMapping("/_delete")
    public CCResult delete(String id) {
        return this.pcbColumnService.delete(id);
    }
}
