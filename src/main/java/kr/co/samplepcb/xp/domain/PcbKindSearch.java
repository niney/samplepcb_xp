package kr.co.samplepcb.xp.domain;

import kr.co.samplepcb.xp.pojo.ElasticIndexName;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.elasticsearch.annotations.*;

import java.util.Date;

@Document(indexName = ElasticIndexName.PCB_KIND)
public class PcbKindSearch implements Persistable<String> {

    @Id
    private String id;
    @Field(type = FieldType.Keyword)
    private String pId;
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ngram_analyzer_case_insensitive", fielddata = true),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(suffix = "normalize", type = FieldType.Keyword, normalizer = "keyword_normalizer")
            }
    )
    private String itemName;
    @Field(type = FieldType.Keyword)
    private Integer target;
    @CreatedDate
    private Date writeDate;
    @LastModifiedDate
    private Date lastModifiedDate;
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ngram_analyzer_case_insensitive", fielddata = true),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(suffix = "normalize", type = FieldType.Keyword, normalizer = "keyword_normalizer")
            }
    )
    private String displayName;
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ngram_analyzer_case_insensitive", fielddata = true),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(suffix = "normalize", type = FieldType.Keyword, normalizer = "keyword_normalizer")
            }
    )
    private String etc1;
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ngram_analyzer_case_insensitive", fielddata = true),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(suffix = "normalize", type = FieldType.Keyword, normalizer = "keyword_normalizer")
            }
    )
    private String etc2;
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ngram_analyzer_case_insensitive", fielddata = true),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(suffix = "normalize", type = FieldType.Keyword, normalizer = "keyword_normalizer")
            }
    )
    private String etc3;


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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEtc1() {
        return etc1;
    }

    public void setEtc1(String etc1) {
        this.etc1 = etc1;
    }

    public String getEtc2() {
        return etc2;
    }

    public void setEtc2(String etc2) {
        this.etc2 = etc2;
    }

    public String getEtc3() {
        return etc3;
    }

    public void setEtc3(String etc3) {
        this.etc3 = etc3;
    }

    @Override
    public String toString() {
        return "PcbKindSearch{" +
                "id='" + id + '\'' +
                ", pId='" + pId + '\'' +
                ", itemName='" + itemName + '\'' +
                ", target=" + target +
                ", writeDate=" + writeDate +
                ", lastModifiedDate=" + lastModifiedDate +
                ", displayName='" + displayName + '\'' +
                ", etc1='" + etc1 + '\'' +
                ", etc2='" + etc2 + '\'' +
                ", etc3='" + etc3 + '\'' +
                '}';
    }
}
