package com.zycus.pims.common.dto;

import java.math.BigDecimal;

public class GoodsReceiptItemSyncDto {
    private Long productId;
    private String productSku;
    private Integer quantityReceived;
    private BigDecimal unitPrice;

    public GoodsReceiptItemSyncDto() {
    }

    public GoodsReceiptItemSyncDto(Long productId, String productSku, Integer quantityReceived, BigDecimal unitPrice) {
        this.productId = productId;
        this.productSku = productSku;
        this.quantityReceived = quantityReceived;
        this.unitPrice = unitPrice;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductSku() {
        return productSku;
    }

    public void setProductSku(String productSku) {
        this.productSku = productSku;
    }

    public Integer getQuantityReceived() {
        return quantityReceived;
    }

    public void setQuantityReceived(Integer quantityReceived) {
        this.quantityReceived = quantityReceived;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
}
