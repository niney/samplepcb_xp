package kr.co.samplepcb.xp.domain;

import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;

public class PcbUnitSearch {

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "samplepcb_analyzer", fielddata = true),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(suffix = "ngram", type = FieldType.Text, analyzer = "ngram_analyzer_case_insensitive", fielddata = true)
            }
    )
    private String field1;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "samplepcb_analyzer", fielddata = true),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(suffix = "ngram", type = FieldType.Text, analyzer = "ngram_analyzer_case_insensitive", fielddata = true)
            }
    )
    private String field2;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "samplepcb_analyzer", fielddata = true),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(suffix = "ngram", type = FieldType.Text, analyzer = "ngram_analyzer_case_insensitive", fielddata = true)
            }
    )
    private String field3;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "samplepcb_analyzer", fielddata = true),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(suffix = "ngram", type = FieldType.Text, analyzer = "ngram_analyzer_case_insensitive", fielddata = true)
            }
    )
    private String field4;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "samplepcb_analyzer", fielddata = true),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(suffix = "ngram", type = FieldType.Text, analyzer = "ngram_analyzer_case_insensitive", fielddata = true)
            }
    )
    private String field5;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "samplepcb_analyzer", fielddata = true),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(suffix = "ngram", type = FieldType.Text, analyzer = "ngram_analyzer_case_insensitive", fielddata = true)
            }
    )
    private String field6;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "samplepcb_analyzer", fielddata = true),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(suffix = "ngram", type = FieldType.Text, analyzer = "ngram_analyzer_case_insensitive", fielddata = true)
            }
    )
    private String field7;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "samplepcb_analyzer", fielddata = true),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(suffix = "ngram", type = FieldType.Text, analyzer = "ngram_analyzer_case_insensitive", fielddata = true)
            }
    )
    private String field8;

    public String getField1() {
        return field1;
    }

    public void setField1(String field1) {
        this.field1 = field1;
    }

    public String getField2() {
        return field2;
    }

    public void setField2(String field2) {
        this.field2 = field2;
    }

    public String getField3() {
        return field3;
    }

    public void setField3(String field3) {
        this.field3 = field3;
    }

    public String getField4() {
        return field4;
    }

    public void setField4(String field4) {
        this.field4 = field4;
    }

    public String getField5() {
        return field5;
    }

    public void setField5(String field5) {
        this.field5 = field5;
    }

    public String getField6() {
        return field6;
    }

    public void setField6(String field6) {
        this.field6 = field6;
    }

    public String getField7() {
        return field7;
    }

    public void setField7(String field7) {
        this.field7 = field7;
    }

    public String getField8() {
        return field8;
    }

    public void setField8(String field8) {
        this.field8 = field8;
    }
}
