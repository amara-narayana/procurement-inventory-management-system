package com.zycus.pims.procurement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public class CreatePurchaseOrderRequest {

    private Long requisitionId;

    @NotNull(message = "Supplier ID is required")
    private Long supplierId;

    private LocalDateTime expectedDeliveryDate;

    private String notes;

    @NotEmpty(message = "Purchase order must contain at least one item")
    @Valid
    private List<PurchaseOrderItemRequest> items;

    public CreatePurchaseOrderRequest() {
    }

    public CreatePurchaseOrderRequest(Long requisitionId, Long supplierId, LocalDateTime expectedDeliveryDate, String notes, List<PurchaseOrderItemRequest> items) {
        this.requisitionId = requisitionId;
        this.supplierId = supplierId;
        this.expectedDeliveryDate = expectedDeliveryDate;
        this.notes = notes;
        this.items = items;
    }

    public Long getRequisitionId() {
        return requisitionId;
    }

    public void setRequisitionId(Long requisitionId) {
        this.requisitionId = requisitionId;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public LocalDateTime getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }

    public void setExpectedDeliveryDate(LocalDateTime expectedDeliveryDate) {
        this.expectedDeliveryDate = expectedDeliveryDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<PurchaseOrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<PurchaseOrderItemRequest> items) {
        this.items = items;
    }
}
