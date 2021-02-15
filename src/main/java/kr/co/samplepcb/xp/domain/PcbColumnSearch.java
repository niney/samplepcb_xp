package kr.co.samplepcb.xp.domain;

import kr.co.samplepcb.xp.pojo.ElasticIndexName;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Document(indexName = ElasticIndexName.PCB_COLUMN)
public class PcbColumnSearch {

    @Id
    private String id;
    @Field(type = FieldType.Text, analyzer = "ngram_analyzer_case_insensitive", fielddata = true)
    private String colName;
    @Field(type = FieldType.Keyword)
    private Integer target;
    private List<Double> colNameVector;
    private Double glScore;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public List<Double> getColNameVector() {
        return colNameVector;
    }

    public void setColNameVector(List<Double> colNameVector) {
        this.colNameVector = colNameVector;
    }

    public Double getGlScore() {
        return glScore;
    }

    public void setGlScore(Double glScore) {
        this.glScore = glScore;
    }

    @Override
    public String toString() {
        return "PcbColumnSearch{" +
                "id='" + id + '\'' +
                ", colName='" + colName + '\'' +
                ", target=" + target +
                ", colNameVector=" + colNameVector +
                ", glScore=" + glScore +
                '}';
    }
}
