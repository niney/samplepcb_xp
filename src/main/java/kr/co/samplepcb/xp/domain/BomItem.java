package kr.co.samplepcb.xp.domain;

import java.util.List;

public class BomItem {

    private int idx;
    private String partName;
    private String imageUrl;
    private Integer moq;
    private Integer purchaseStock;
    private Integer unitPrice;
    private Integer calcPrice;
    private String providerName;
    private String orderDate;
    private String stockingDate;
    private String orderStatus;
    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getMoq() {
        return moq;
    }

    public void setMoq(Integer moq) {
        this.moq = moq;
    }

    public Integer getPurchaseStock() {
        return purchaseStock;
    }

    public void setPurchaseStock(Integer purchaseStock) {
        this.purchaseStock = purchaseStock;
    }

    public Integer getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Integer unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getCalcPrice() {
        return calcPrice;
    }

    public void setCalcPrice(Integer calcPrice) {
        this.calcPrice = calcPrice;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getStockingDate() {
        return stockingDate;
    }

    public void setStockingDate(String stockingDate) {
        this.stockingDate = stockingDate;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }
}
