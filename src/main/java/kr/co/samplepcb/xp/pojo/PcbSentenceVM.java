package kr.co.samplepcb.xp.pojo;

import java.util.List;

public class PcbSentenceVM {

    private List<String> queryColumnNameList;
    private List<PcbColumnSearchVM> pcbColumnSearchList;
    private Double averageScore;

    public List<String> getQueryColumnNameList() {
        return queryColumnNameList;
    }

    public void setQueryColumnNameList(List<String> queryColumnNameList) {
        this.queryColumnNameList = queryColumnNameList;
    }

    public List<PcbColumnSearchVM> getPcbColumnSearchList() {
        return pcbColumnSearchList;
    }

    public void setPcbColumnSearchList(List<PcbColumnSearchVM> pcbColumnSearchList) {
        this.pcbColumnSearchList = pcbColumnSearchList;
    }

    public Double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(Double averageScore) {
        this.averageScore = averageScore;
    }
}
