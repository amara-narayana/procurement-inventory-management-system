package com.zycus.pims.procurement.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class GoodsReceiptItemRequest {

    @NotNull(message = "Purchase order item ID is required")
    private Long poItemId;

    @NotNull(message = "Quantity received is required")
    @Min(value = 1, message = "Received quantity must be at least 1")
    private Integer quantityReceived;

    private String notes;

    public GoodsReceiptItemRequest() {
    }

    public GoodsReceiptItemRequest(Long poItemId, Integer quantityReceived, String notes) {
        this.poItemId = poItemId;
        this.quantityReceived = quantityReceived;
        this.notes = notes;
    }

    public Long getPoItemId() {
        return poItemId;
    }

    public void setPoItemId(Long poItemId) {
        this.poItemId = poItemId;
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
