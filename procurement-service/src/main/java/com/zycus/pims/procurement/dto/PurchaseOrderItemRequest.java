package com.zycus.pims.procurement.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class PurchaseOrderItemRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotBlank(message = "Product SKU is required")
    private String productSku;

    @NotBlank(message = "Product name is required")
    private String productName;

    @NotNull(message = "Quantity ordered is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantityOrdered;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.01", message = "Unit price must be greater than zero")
    private BigDecimal unitPrice;

    public PurchaseOrderItemRequest() {
    }

    public PurchaseOrderItemRequest(Long productId, String productSku, String productName, Integer quantityOrdered, BigDecimal unitPrice) {
        this.productId = productId;
        this.productSku = productSku;
        this.productName = productName;
        this.quantityOrdered = quantityOrdered;
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

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQuantityOrdered() {
        return quantityOrdered;
    }

    public void setQuantityOrdered(Integer quantityOrdered) {
        this.quantityOrdered = quantityOrdered;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
}
