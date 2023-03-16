package kr.co.samplepcb.xp.resource;

import coolib.common.CCResult;
import kr.co.samplepcb.xp.domain.BomItem;
import kr.co.samplepcb.xp.pojo.BomQueryParam;
import kr.co.samplepcb.xp.service.ExcelDownloadView;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/bom")
public class BomResource {
    @PostMapping(value = "/orderDetail/_downloadExcel", produces = {"application/vnd.ms-excel"})
    public Object orderDetailDownloadExcel(BomQueryParam bomQueryParam) {
        List<BomItem> bomItemList = bomQueryParam.getBomItemList();
        if (CollectionUtils.isEmpty(bomItemList)) {
            return CCResult.dataNotFound();
        }
        return new ModelAndView(ExcelDownloadView.VIEW_NAME, Collections.singletonMap(ExcelDownloadView.BOM_ORDER_DETAIL, bomQueryParam));
    }
}
