package com.zycus.pims.common.dto;

import java.util.List;

public class GoodsReceiptSyncRequest {
    private String receiptNumber;
    private String purchaseOrderNumber;
    private Long warehouseId;
    private List<GoodsReceiptItemSyncDto> items;
    private String receivedBy;

    public GoodsReceiptSyncRequest() {
    }

    public GoodsReceiptSyncRequest(String receiptNumber, String purchaseOrderNumber, Long warehouseId, List<GoodsReceiptItemSyncDto> items, String receivedBy) {
        this.receiptNumber = receiptNumber;
        this.purchaseOrderNumber = purchaseOrderNumber;
        this.warehouseId = warehouseId;
        this.items = items;
        this.receivedBy = receivedBy;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public String getPurchaseOrderNumber() {
        return purchaseOrderNumber;
    }

    public void setPurchaseOrderNumber(String purchaseOrderNumber) {
        this.purchaseOrderNumber = purchaseOrderNumber;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public List<GoodsReceiptItemSyncDto> getItems() {
        return items;
    }

    public void setItems(List<GoodsReceiptItemSyncDto> items) {
        this.items = items;
    }

    public String getReceivedBy() {
        return receivedBy;
    }

    public void setReceivedBy(String receivedBy) {
        this.receivedBy = receivedBy;
    }
}
