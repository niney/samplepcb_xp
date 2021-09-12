package kr.co.samplepcb.xp.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

public class PcbPartSpec {

    @Field(type = FieldType.Nested)
    private PcbPartAttribute attribute;
    @Field(type = FieldType.Keyword, normalizer = "keyword_normalizer", value = "display_value")
    @JsonProperty("display_value")
    private String displayValue;

    public PcbPartAttribute getAttribute() {
        return attribute;
    }

    public void setAttribute(PcbPartAttribute attribute) {
        this.attribute = attribute;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public void setDisplayValue(String displayValue) {
        this.displayValue = displayValue;
    }

    public static class PcbPartAttribute {
        @Field(type = FieldType.Keyword, normalizer = "keyword_normalizer")
        String group;
        @Field(type = FieldType.Keyword, normalizer = "keyword_normalizer")
        String name;
        @Field(type = FieldType.Keyword, normalizer = "keyword_normalizer")
        String shortname;

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getShortname() {
            return shortname;
        }

        public void setShortname(String shortname) {
            this.shortname = shortname;
        }
    }
}
