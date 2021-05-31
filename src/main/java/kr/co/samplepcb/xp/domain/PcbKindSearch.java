package kr.co.samplepcb.xp.domain;

import kr.co.samplepcb.xp.pojo.ElasticIndexName;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.Instant;
import java.util.Date;

@Document(indexName = ElasticIndexName.PCB_KIND)
public class PcbKindSearch implements Persistable<String> {

    @Id
    private String id;
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ngram_analyzer_case_insensitive", fielddata = true),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword)
            }
    )
    private String itemName;
    @Field(type = FieldType.Keyword)
    private Integer target;
    @CreatedDate
    private Date writeDate;
    @LastModifiedDate
    private Date lastModifiedDate;

    @Override
    public boolean isNew() {
        return id == null;
    }

    @Override
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

    public Integer getTarget() {
        return target;
    }

    public void setTarget(Integer target) {
        this.target = target;
    }

    public Date getWriteDate() {
        return writeDate;
    }

    public void setWriteDate(Date writeDate) {
        this.writeDate = writeDate;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    @Override
    public String toString() {
        return "PcbKindSearch{" +
                "id='" + id + '\'' +
                ", itemName='" + itemName + '\'' +
                ", target=" + target +
                ", writeDate=" + writeDate +
                ", lastModifiedDate=" + lastModifiedDate +
                '}';
    }
}
