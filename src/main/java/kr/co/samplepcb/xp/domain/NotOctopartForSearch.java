package kr.co.samplepcb.xp.domain;

import kr.co.samplepcb.xp.pojo.ElasticIndexName;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

@Document(indexName = ElasticIndexName.NOT_OCTOPART)
public class NotOctopartForSearch {

    @Id
    private String id;
    @MultiField(
            mainField = @Field(type = FieldType.Keyword, normalizer = "keyword_normalizer"),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(suffix = "ngram4", type = FieldType.Text, analyzer = "ngram_analyzer4_case_insensitive", fielddata = true)
            }
    )
    private String mpn;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMpn() {
        return mpn;
    }

    public void setMpn(String mpn) {
        this.mpn = mpn;
    }
}
