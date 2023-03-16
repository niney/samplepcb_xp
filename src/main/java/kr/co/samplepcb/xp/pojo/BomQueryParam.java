package kr.co.samplepcb.xp.pojo;

import coolib.common.QueryParam;
import kr.co.samplepcb.xp.domain.BomItem;

import java.util.List;

public class BomQueryParam extends QueryParam {

    private String itId;
    private List<BomItem> bomItemList;

    public String getItId() {
        return itId;
    }

    public void setItId(String itId) {
        this.itId = itId;
    }

    public List<BomItem> getBomItemList() {
        return bomItemList;
    }

    public void setBomItemList(List<BomItem> bomItemList) {
        this.bomItemList = bomItemList;
    }
}
