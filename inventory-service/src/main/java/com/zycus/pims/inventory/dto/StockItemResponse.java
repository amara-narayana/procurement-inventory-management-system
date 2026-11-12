package com.zycus.pims.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class StockItemResponse {
    private Long id;
    private Long productId;
    private String productSku;
    private String productName;
    private String categoryName;
    private String unitOfMeasurement;
    private BigDecimal unitPrice;
    private Long warehouseId;
    private String warehouseName;
    private String warehouseLocation;
    private Integer quantity;
    private BigDecimal totalValue;
    private boolean lowStock;
    private LocalDateTime updatedAt;

    public StockItemResponse() {
    }

    public StockItemResponse(Long id, Long productId, String productSku, String productName, String categoryName, String unitOfMeasurement, BigDecimal unitPrice, Long warehouseId, String warehouseName, String warehouseLocation, Integer quantity, BigDecimal totalValue, boolean lowStock, LocalDateTime updatedAt) {
        this.id = id;
        this.productId = productId;
        this.productSku = productSku;
        this.productName = productName;
        this.categoryName = categoryName;
        this.unitOfMeasurement = unitOfMeasurement;
        this.unitPrice = unitPrice;
        this.warehouseId = warehouseId;
        this.warehouseName = warehouseName;
        this.warehouseLocation = warehouseLocation;
        this.quantity = quantity;
        this.totalValue = totalValue;
        this.lowStock = lowStock;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getUnitOfMeasurement() {
        return unitOfMeasurement;
    }

    public void setUnitOfMeasurement(String unitOfMeasurement) {
        this.unitOfMeasurement = unitOfMeasurement;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    public String getWarehouseLocation() {
        return warehouseLocation;
    }

    public void setWarehouseLocation(String warehouseLocation) {
        this.warehouseLocation = warehouseLocation;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }

    public boolean isLowStock() {
        return lowStock;
    }

    public void setLowStock(boolean lowStock) {
        this.lowStock = lowStock;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
