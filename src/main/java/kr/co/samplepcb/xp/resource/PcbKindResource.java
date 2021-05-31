package kr.co.samplepcb.xp.resource;

import coolib.common.CCObjectResult;
import coolib.common.CCResult;
import coolib.common.QueryParam;
import kr.co.samplepcb.xp.domain.PcbKindSearch;
import kr.co.samplepcb.xp.pojo.PcbKindSearchVM;
import kr.co.samplepcb.xp.service.ExcelDownloadView;
import kr.co.samplepcb.xp.service.PcbKindService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collections;

@RestController
@RequestMapping("/api/pcbKind")
public class PcbKindResource {

    // search
    private final ElasticsearchOperations elasticsearchOperations;

    // service
    private final PcbKindService pcbKindService;

    public PcbKindResource(ElasticsearchOperations elasticsearchOperations, PcbKindService pcbKindService) {
        this.elasticsearchOperations = elasticsearchOperations;
        this.pcbKindService = pcbKindService;
    }

    @GetMapping("/_reindexAll")
    public CCResult reindexAll() {
        IndexOperations indexOperations = this.elasticsearchOperations.indexOps(PcbKindSearch.class);
        Document document = indexOperations.createMapping();
        indexOperations.putMapping(document);
        return CCResult.ok();
    }

    @GetMapping("/_search")
    public CCResult search(@PageableDefault Pageable pageable, QueryParam queryParam, PcbKindSearchVM pcbKindSearchVM) {
        return this.pcbKindService.search(pageable, queryParam, pcbKindSearchVM);
    }

    @PostMapping("/_indexing")
    public CCResult indexing(PcbKindSearchVM pcbItemSearchVM) {
        return this.pcbKindService.indexing(pcbItemSearchVM);
    }

    @GetMapping("/_delete")
    public CCResult delete(String id) {
        return this.pcbKindService.delete(id);
    }

    @GetMapping(value = "/_downloadExcel", produces = {"application/vnd.ms-excel"})
    public Object downloadExcel() {
        return new ModelAndView(ExcelDownloadView.VIEW_NAME, Collections.singletonMap(ExcelDownloadView.ALL_KIND_GROUP_BY_TARGET, this.pcbKindService.getAllItemGroupByTarget()));
    }

    @PostMapping(value = "/_uploadItemFile")
    public CCResult uploadItemFile(@RequestParam("file") MultipartFile file/*, HttpServletRequest request*/) {
        this.pcbKindService.reindexAllByFile(file);
        return CCResult.ok();
    }

    @GetMapping("/_allItemGroupByTarget")
    public CCResult allItemGroupByTarget() {
        return CCObjectResult.setSimpleData(this.pcbKindService.getAllItemGroupByTarget());
    }

}
