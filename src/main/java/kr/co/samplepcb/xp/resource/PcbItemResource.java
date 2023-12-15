package kr.co.samplepcb.xp.resource;

import coolib.common.CCObjectResult;
import coolib.common.CCResult;
import coolib.common.QueryParam;
import kr.co.samplepcb.xp.domain.PcbItemSearch;
import kr.co.samplepcb.xp.pojo.PcbItemSearchVM;
import kr.co.samplepcb.xp.service.ExcelDownloadView;
import kr.co.samplepcb.xp.service.PcbItemService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/pcbItem")
public class PcbItemResource {

    // search
    private final ElasticsearchOperations elasticsearchOperations;

    // service
    private final PcbItemService pcbItemService;

    public PcbItemResource(ElasticsearchOperations elasticsearchOperations, PcbItemService pcbItemService) {
        this.elasticsearchOperations = elasticsearchOperations;
        this.pcbItemService = pcbItemService;
    }

    @GetMapping("/_reindexAll")
    public CCResult reindexAll() {
        IndexOperations indexOperations = this.elasticsearchOperations.indexOps(PcbItemSearch.class);
        Document document = indexOperations.createMapping();
        indexOperations.putMapping(document);
        this.pcbItemService.reindexAll();

        return CCResult.ok();
    }

    @GetMapping("/_search")
    public CCResult search(@PageableDefault Pageable pageable, QueryParam queryParam, PcbItemSearchVM pcbColumnSearchVM) {
        return this.pcbItemService.search(pageable, queryParam, pcbColumnSearchVM);
    }

    @PostMapping("/_searchList")
    public CCResult searchList(@PageableDefault Pageable pageable, QueryParam queryParam, int target, @RequestBody List<String> pcbItemNameList) {
        return this.pcbItemService.searchList(pageable, queryParam, target, pcbItemNameList);
    }

    @PostMapping("/_indexing")
    public CCResult indexing(PcbItemSearchVM pcbItemSearchVM) {
        return this.pcbItemService.indexing(pcbItemSearchVM);
    }

    @GetMapping("/_delete")
    public CCResult delete(String id) {
        return this.pcbItemService.delete(id);
    }

    @GetMapping(value = "/_downloadExcel", produces = {"application/vnd.ms-excel"})
    public Object downloadExcel() {
        return new ModelAndView(ExcelDownloadView.VIEW_NAME, Collections.singletonMap(ExcelDownloadView.ALL_ITEM_GROUP_BY_TARGET, this.pcbItemService.getAllItemGroupByTarget()));
    }

    @PostMapping(value = "/_uploadItemFile")
    public CCResult uploadItemFile(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        this.pcbItemService.reindexAllByFile(file);
        return CCResult.ok();
    }

    @GetMapping("/_allItemGroupByTarget")
    public CCResult allItemGroupByTarget() {
        return CCObjectResult.setSimpleData(this.pcbItemService.getAllItemGroupByTarget());
    }

}
