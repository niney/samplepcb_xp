package kr.co.samplepcb.xp.pojo;

import kr.co.samplepcb.xp.domain.PcbPartsSearch;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PcbPartsSearchVM {

    public final static List<Field> pcbPartsSearchFields = new ArrayList<>();
    static {
        ReflectionUtils.doWithFields(PcbPartsSearchVM.class, field -> {
            if(field.getType() == Integer.class ||
                    field.getType() == String.class ||
                    field.getType() == Date.class ) {
                field.setAccessible(true);
                pcbPartsSearchFields.add(field);
            }
        });
    }

    private String id;
    private String largeCategory;
    private String mediumCategory;
    private String smallCategory;
    private String partName;
    private String description;
    private String manufacturerName;
    private String partsPackaging;
    private String packaging;
    private Integer moq;
    private Integer price;
    private Integer price1to10;
    private Integer price11to50;
    private Integer price51to100;
    private Integer price101to500;
    private Integer price501to1000;
    private Integer inventoryLevel;
    private String memo;
    private String offerName;
    private List<PcbImageVM> images;
    private String managerPhoneNumber;
    private String managerName;
    private String managerEmail;
    private Integer status;
    private List<Integer> statusList;
    private String token;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public static List<Field> getPcbPartsSearchFields() {
        return pcbPartsSearchFields;
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

    public List<Integer> getStatusList() {
        return statusList;
    }

    public void setStatusList(List<Integer> statusList) {
        this.statusList = statusList;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
