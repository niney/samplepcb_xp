package kr.co.samplepcb.xp.pojo;

import java.util.List;

public class PcbKindSearchVM {

    private String id;
    private String pId;
    private String itemName;
    private Integer target;
    private List<Integer> targetList;
    private String displayName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Integer getTarget() {
        return target;
    }

    public void setTarget(Integer target) {
        this.target = target;
    }

    public List<Integer> getTargetList() {
        return targetList;
    }

    public void setTargetList(List<Integer> targetList) {
        this.targetList = targetList;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
