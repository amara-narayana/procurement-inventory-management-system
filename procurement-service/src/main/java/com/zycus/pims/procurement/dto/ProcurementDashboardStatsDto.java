package com.zycus.pims.procurement.dto;

import java.math.BigDecimal;
import java.util.Map;

public class ProcurementDashboardStatsDto {
    private long totalSuppliers;
    private long pendingRequisitions;
    private long openPurchaseOrders;
    private BigDecimal totalSpendAmount;
    private Map<String, Long> requisitionsByStatus;
    private Map<String, Long> ordersByStatus;
    private Map<String, BigDecimal> spendBySupplier;

    public ProcurementDashboardStatsDto() {
    }

    public ProcurementDashboardStatsDto(long totalSuppliers, long pendingRequisitions, long openPurchaseOrders, BigDecimal totalSpendAmount, Map<String, Long> requisitionsByStatus, Map<String, Long> ordersByStatus, Map<String, BigDecimal> spendBySupplier) {
        this.totalSuppliers = totalSuppliers;
        this.pendingRequisitions = pendingRequisitions;
        this.openPurchaseOrders = openPurchaseOrders;
        this.totalSpendAmount = totalSpendAmount;
        this.requisitionsByStatus = requisitionsByStatus;
        this.ordersByStatus = ordersByStatus;
        this.spendBySupplier = spendBySupplier;
    }

    public long getTotalSuppliers() {
        return totalSuppliers;
    }

    public void setTotalSuppliers(long totalSuppliers) {
        this.totalSuppliers = totalSuppliers;
    }

    public long getPendingRequisitions() {
        return pendingRequisitions;
    }

    public void setPendingRequisitions(long pendingRequisitions) {
        this.pendingRequisitions = pendingRequisitions;
    }

    public long getOpenPurchaseOrders() {
        return openPurchaseOrders;
    }

    public void setOpenPurchaseOrders(long openPurchaseOrders) {
        this.openPurchaseOrders = openPurchaseOrders;
    }

    public BigDecimal getTotalSpendAmount() {
        return totalSpendAmount;
    }

    public void setTotalSpendAmount(BigDecimal totalSpendAmount) {
        this.totalSpendAmount = totalSpendAmount;
    }

    public Map<String, Long> getRequisitionsByStatus() {
        return requisitionsByStatus;
    }

    public void setRequisitionsByStatus(Map<String, Long> requisitionsByStatus) {
        this.requisitionsByStatus = requisitionsByStatus;
    }

    public Map<String, Long> getOrdersByStatus() {
        return ordersByStatus;
    }

    public void setOrdersByStatus(Map<String, Long> ordersByStatus) {
        this.ordersByStatus = ordersByStatus;
    }

    public Map<String, BigDecimal> getSpendBySupplier() {
        return spendBySupplier;
    }

    public void setSpendBySupplier(Map<String, BigDecimal> spendBySupplier) {
        this.spendBySupplier = spendBySupplier;
    }
}
