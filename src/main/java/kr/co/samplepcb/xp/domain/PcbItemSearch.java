package kr.co.samplepcb.xp.domain;

import kr.co.samplepcb.xp.pojo.ElasticIndexName;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = ElasticIndexName.PCB_ITEM)
public class PcbItemSearch {

    @Id
    private String id;
    @Field(type = FieldType.Keyword)
    private String itemName;
    @Field(type = FieldType.Text, analyzer = "ngram_analyzer_case_insensitive", fielddata = true)
    private String itemNameText;
    @Field(type = FieldType.Keyword)
    private Integer target;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemNameText() {
        return itemNameText;
    }

    public void setItemNameText(String itemNameText) {
        this.itemNameText = itemNameText;
    }

    public Integer getTarget() {
        return target;
    }

    public void setTarget(Integer target) {
        this.target = target;
    }
}
