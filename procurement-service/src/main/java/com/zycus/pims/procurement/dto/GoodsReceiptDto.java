package com.zycus.pims.procurement.dto;

import java.time.LocalDateTime;
import java.util.List;

public class GoodsReceiptDto {
    private Long id;
    private String receiptNumber;
    private Long purchaseOrderId;
    private String poNumber;
    private Long warehouseId;
    private LocalDateTime receivedDate;
    private String receivedBy;
    private String notes;
    private List<GoodsReceiptItemDto> items;
    private LocalDateTime createdAt;

    public GoodsReceiptDto() {
    }

    public GoodsReceiptDto(Long id, String receiptNumber, Long purchaseOrderId, String poNumber, Long warehouseId, LocalDateTime receivedDate, String receivedBy, String notes, List<GoodsReceiptItemDto> items, LocalDateTime createdAt) {
        this.id = id;
        this.receiptNumber = receiptNumber;
        this.purchaseOrderId = purchaseOrderId;
        this.poNumber = poNumber;
        this.warehouseId = warehouseId;
        this.receivedDate = receivedDate;
        this.receivedBy = receivedBy;
        this.notes = notes;
        this.items = items;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public Long getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public void setPurchaseOrderId(Long purchaseOrderId) {
        this.purchaseOrderId = purchaseOrderId;
    }

    public String getPoNumber() {
        return poNumber;
    }

    public void setPoNumber(String poNumber) {
        this.poNumber = poNumber;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public LocalDateTime getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(LocalDateTime receivedDate) {
        this.receivedDate = receivedDate;
    }

    public String getReceivedBy() {
        return receivedBy;
    }

    public void setReceivedBy(String receivedBy) {
        this.receivedBy = receivedBy;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<GoodsReceiptItemDto> getItems() {
        return items;
    }

    public void setItems(List<GoodsReceiptItemDto> items) {
        this.items = items;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
