package com.zycus.pims.procurement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class CreateGoodsReceiptRequest {

    @NotNull(message = "Warehouse ID is required")
    private Long warehouseId;

    private String notes;

    @NotEmpty(message = "Receipt must include at least one item")
    @Valid
    private List<GoodsReceiptItemRequest> items;

    public CreateGoodsReceiptRequest() {
    }

    public CreateGoodsReceiptRequest(Long warehouseId, String notes, List<GoodsReceiptItemRequest> items) {
        this.warehouseId = warehouseId;
        this.notes = notes;
        this.items = items;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<GoodsReceiptItemRequest> getItems() {
        return items;
    }

    public void setItems(List<GoodsReceiptItemRequest> items) {
        this.items = items;
    }
}
