package kr.co.samplepcb.xp.domain;

import kr.co.samplepcb.xp.pojo.ElasticIndexName;
import kr.co.samplepcb.xp.pojo.PcbImageVM;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.elasticsearch.annotations.*;

import java.util.Date;
import java.util.List;

@Document(indexName = ElasticIndexName.PCB_PARTS)
public class PcbPartsSearch implements Persistable<String> {

    @Id
    private String id;
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
    private String largeCategory;
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ngram_analyzer_case_insensitive", fielddata = true),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(suffix = "normalize", type = FieldType.Keyword, normalizer = "keyword_normalizer")
            }
    )
    private String mediumCategory;
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ngram_analyzer_case_insensitive", fielddata = true),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(suffix = "normalize", type = FieldType.Keyword, normalizer = "keyword_normalizer")
            }
    )
    private String smallCategory;
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ngram_analyzer4_case_insensitive", fielddata = true),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(suffix = "normalize", type = FieldType.Keyword, normalizer = "keyword_normalizer")
            }
    )
    private String partName;
    @Field(type = FieldType.Text, analyzer = "nori")
    private String description;
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ngram_analyzer_case_insensitive", fielddata = true),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(suffix = "normalize", type = FieldType.Keyword, normalizer = "keyword_normalizer")
            }
    )
    private String manufacturerName;
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ngram_analyzer_case_insensitive", fielddata = true),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(suffix = "normalize", type = FieldType.Keyword, normalizer = "keyword_normalizer")
            }
    )
    private String partsPackaging;
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ngram_analyzer_case_insensitive", fielddata = true),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(suffix = "normalize", type = FieldType.Keyword, normalizer = "keyword_normalizer")
            }
    )
    private String packaging;
    @Field(type = FieldType.Integer)
    private Integer moq;

    @Field(type = FieldType.Integer)
    private Integer price;
    @Field(type = FieldType.Integer)
    private Integer price1to10;
    @Field(type = FieldType.Integer)
    private Integer price11to50;
    @Field(type = FieldType.Integer)
    private Integer price51to100;
    @Field(type = FieldType.Integer)
    private Integer price101to500;
    @Field(type = FieldType.Integer)
    private Integer price501to1000;

    @Field(type = FieldType.Integer)
    private Integer inventoryLevel;
    @Field(type = FieldType.Text, analyzer = "nori")
    private String memo;
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ngram_analyzer_case_insensitive", fielddata = true),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword)
            }
    )
    private String offerName;
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ngram_analyzer_case_insensitive", fielddata = true),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword)
            }
    )
    private String managerPhoneNumber;
    @Field(type = FieldType.Nested)
    private List<PcbImageVM> images;
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ngram_analyzer_case_insensitive", fielddata = true),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword)
            }
    )
    private String managerName;
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ngram_analyzer_case_insensitive", fielddata = true),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword)
            }
    )
    private String managerEmail;
    private Integer status;

    public String getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return this.id == null;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getLargeCategory() {
        return largeCategory;
    }

    public void setLargeCategory(String largeCategory) {
        this.largeCategory = largeCategory;
    }

    public String getMediumCategory() {
        return mediumCategory;
    }

    public void setMediumCategory(String mediumCategory) {
        this.mediumCategory = mediumCategory;
    }

    public String getSmallCategory() {
        return smallCategory;
    }

    public void setSmallCategory(String smallCategory) {
        this.smallCategory = smallCategory;
    }

    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getManufacturerName() {
        return manufacturerName;
    }

    public void setManufacturerName(String manufacturerName) {
        this.manufacturerName = manufacturerName;
    }

    public String getPartsPackaging() {
        return partsPackaging;
    }

    public void setPartsPackaging(String partsPackaging) {
        this.partsPackaging = partsPackaging;
    }

    public String getPackaging() {
        return packaging;
    }

    public void setPackaging(String packaging) {
        this.packaging = packaging;
    }

    public Integer getMoq() {
        return moq;
    }

    public void setMoq(Integer moq) {
        this.moq = moq;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getPrice1to10() {
        return price1to10;
    }

    public void setPrice1to10(Integer price1to10) {
        this.price1to10 = price1to10;
    }

    public Integer getPrice11to50() {
        return price11to50;
    }

    public void setPrice11to50(Integer price11to50) {
        this.price11to50 = price11to50;
    }

    public Integer getPrice51to100() {
        return price51to100;
    }

    public void setPrice51to100(Integer price51to100) {
        this.price51to100 = price51to100;
    }

    public Integer getPrice101to500() {
        return price101to500;
    }

    public void setPrice101to500(Integer price101to500) {
        this.price101to500 = price101to500;
    }

    public Integer getPrice501to1000() {
        return price501to1000;
    }

    public void setPrice501to1000(Integer price501to1000) {
        this.price501to1000 = price501to1000;
    }

    public Integer getInventoryLevel() {
        return inventoryLevel;
    }

    public void setInventoryLevel(Integer inventoryLevel) {
        this.inventoryLevel = inventoryLevel;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getOfferName() {
        return offerName;
    }

    public void setOfferName(String offerName) {
        this.offerName = offerName;
    }

    public List<PcbImageVM> getImages() {
        return images;
    }

    public void setImages(List<PcbImageVM> images) {
        this.images = images;
    }

    public String getManagerPhoneNumber() {
        return managerPhoneNumber;
    }

    public void setManagerPhoneNumber(String managerPhoneNumber) {
        this.managerPhoneNumber = managerPhoneNumber;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public String getManagerEmail() {
        return managerEmail;
    }

    public void setManagerEmail(String managerEmail) {
        this.managerEmail = managerEmail;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
