package kr.co.samplepcb.xp.pojo;

public class PcbColumnSearchVM {

    private String id;
    private Integer columnIdx;
    private String colName;
    private Integer target;
    private Double glScore;

    private String queryColName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getColumnIdx() {
        return columnIdx;
    }

    public void setColumnIdx(Integer columnIdx) {
        this.columnIdx = columnIdx;
    }

    public String getColName() {
        return colName;
    }

    public void setColName(String colName) {
        this.colName = colName;
    }

    public Integer getTarget() {
        return target;
    }

    public void setTarget(Integer target) {
        this.target = target;
    }

    public Double getGlScore() {
        return glScore;
    }

    public void setGlScore(Double glScore) {
        this.glScore = glScore;
    }

    public String getQueryColName() {
        return queryColName;
    }

    public void setQueryColName(String queryColName) {
        this.queryColName = queryColName;
    }
}
