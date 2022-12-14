package kr.co.samplepcb.xp.resource;

import coolib.common.CCPagingResult;
import coolib.common.CCResult;
import coolib.common.QueryParam;
import kr.co.samplepcb.xp.domain.PcbPartsSearch;
import kr.co.samplepcb.xp.pojo.PcbPartsSearchField;
import kr.co.samplepcb.xp.pojo.PcbPartsSearchVM;
import kr.co.samplepcb.xp.service.ExcelDownloadView;
import kr.co.samplepcb.xp.service.PcbPartsService;
import kr.co.samplepcb.xp.util.XssSanitizerUtil;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/pcbParts")
public class PcbPartsResource {

    // search
    private final ElasticsearchOperations elasticsearchOperations;

    // service
    private final PcbPartsService pcbPartsService;

    public PcbPartsResource(ElasticsearchOperations elasticsearchOperations, PcbPartsService pcbPartsService) {
        this.elasticsearchOperations = elasticsearchOperations;
        this.pcbPartsService = pcbPartsService;
    }

    @GetMapping("/_reindexAll")
    public CCResult reindexAll() {
        IndexOperations indexOperations = this.elasticsearchOperations.indexOps(PcbPartsSearch.class);
        Document document = indexOperations.createMapping();
        indexOperations.putMapping(document);
        return CCResult.ok();
    }

    @PostMapping(value = "/_indexing", produces = {"application/json", "application/x-www-form-urlencoded"})
    public CCResult indexing(@RequestBody PcbPartsSearchVM pcbPartsSearchVM) {
        if(StringUtils.isNotEmpty(pcbPartsSearchVM.getContents())) {
            // contents xss filter
            pcbPartsSearchVM.setContents(XssSanitizerUtil.stripXSS(pcbPartsSearchVM.getContents()));
        }
        return this.pcbPartsService.indexing(pcbPartsSearchVM);
    }

    @GetMapping("/_search")
    public CCResult search(@PageableDefault @SortDefault.SortDefaults({
            @SortDefault(sort = ScoreSortBuilder.NAME, direction = Sort.Direction.DESC), // 높은 점수
            @SortDefault(sort = PcbPartsSearchField.INVENTORY_LEVEL, direction = Sort.Direction.DESC), // 재고 있음(많음)
            @SortDefault(sort = PcbPartsSearchField.PRICE1, direction = Sort.Direction.ASC) // 낮은 가격
    }) Pageable pageable, QueryParam queryParam, PcbPartsSearchVM pcbPartsSearchVM) {
        if (StringUtils.isNotEmpty(pcbPartsSearchVM.getToken())) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, PcbPartsSearchField.WRITE_DATE));
        }
        return this.pcbPartsService.search(pageable, queryParam, pcbPartsSearchVM);
    }

    @PostMapping("/_search")
    public CCResult postSearch(@PageableDefault @SortDefault.SortDefaults({
            @SortDefault(sort = ScoreSortBuilder.NAME, direction = Sort.Direction.DESC), // 높은 점수
            @SortDefault(sort = PcbPartsSearchField.INVENTORY_LEVEL, direction = Sort.Direction.DESC), // 재고 있음(많음)
            @SortDefault(sort = PcbPartsSearchField.PRICE1, direction = Sort.Direction.ASC) // 낮은 가격
    }) Pageable pageable, QueryParam queryParam, @RequestBody PcbPartsSearchVM pcbPartsSearchVM) {
        if (StringUtils.isNotEmpty(pcbPartsSearchVM.getToken())) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, PcbPartsSearchField.WRITE_DATE));
        }
        return this.pcbPartsService.search(pageable, queryParam, pcbPartsSearchVM);
    }

    @PostMapping("/_delete")
    public CCResult deleteParts(@RequestParam List<String> ids) {
        return this.pcbPartsService.deleteParts(ids);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @GetMapping(value = "/_downloadExcel", produces = {"application/vnd.ms-excel"})
    public Object downloadExcel(QueryParam queryParam, PcbPartsSearchVM pcbPartsSearchVM) {
        CCPagingResult<List> searchResult = (CCPagingResult<List>) this.pcbPartsService.search(PageRequest.of(0, 5000), queryParam, pcbPartsSearchVM);
        return new ModelAndView(ExcelDownloadView.VIEW_NAME, Collections.singletonMap(ExcelDownloadView.ALL_PARTS, searchResult.getData()));
    }

    @PostMapping(value = "/_uploadItemFile")
    public CCResult uploadItemFile(@RequestParam("file") MultipartFile file/*, HttpServletRequest request*/) {
        this.pcbPartsService.reindexAllByFile(file);
        return CCResult.ok();
    }

}
