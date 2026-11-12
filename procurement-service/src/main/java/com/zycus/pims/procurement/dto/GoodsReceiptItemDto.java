package com.zycus.pims.procurement.dto;

public class GoodsReceiptItemDto {
    private Long id;
    private Long poItemId;
    private Long productId;
    private String productSku;
    private Integer quantityReceived;
    private String notes;

    public GoodsReceiptItemDto() {
    }

    public GoodsReceiptItemDto(Long id, Long poItemId, Long productId, String productSku, Integer quantityReceived, String notes) {
        this.id = id;
        this.poItemId = poItemId;
        this.productId = productId;
        this.productSku = productSku;
        this.quantityReceived = quantityReceived;
        this.notes = notes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPoItemId() {
        return poItemId;
    }

    public void setPoItemId(Long poItemId) {
        this.poItemId = poItemId;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
