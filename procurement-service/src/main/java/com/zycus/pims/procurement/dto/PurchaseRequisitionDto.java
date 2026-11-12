package com.zycus.pims.procurement.dto;

import com.zycus.pims.common.enums.RequisitionStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PurchaseRequisitionDto {
    private Long id;
    private String requisitionNumber;
    private String requestingUser;
    private String department;
    private String businessJustification;
    private BigDecimal totalEstimatedAmount;
    private RequisitionStatus status;
    private LocalDateTime submissionDate;
    private String approverUser;
    private LocalDateTime approvalDate;
    private String approvalNotes;
    private List<RequisitionItemDto> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PurchaseRequisitionDto() {
    }

    public PurchaseRequisitionDto(Long id, String requisitionNumber, String requestingUser, String department, String businessJustification, BigDecimal totalEstimatedAmount, RequisitionStatus status, LocalDateTime submissionDate, String approverUser, LocalDateTime approvalDate, String approvalNotes, List<RequisitionItemDto> items, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.requisitionNumber = requisitionNumber;
        this.requestingUser = requestingUser;
        this.department = department;
        this.businessJustification = businessJustification;
        this.totalEstimatedAmount = totalEstimatedAmount;
        this.status = status;
        this.submissionDate = submissionDate;
        this.approverUser = approverUser;
        this.approvalDate = approvalDate;
        this.approvalNotes = approvalNotes;
        this.items = items;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRequisitionNumber() {
        return requisitionNumber;
    }

    public void setRequisitionNumber(String requisitionNumber) {
        this.requisitionNumber = requisitionNumber;
    }

    public String getRequestingUser() {
        return requestingUser;
    }

    public void setRequestingUser(String requestingUser) {
        this.requestingUser = requestingUser;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getBusinessJustification() {
        return businessJustification;
    }

    public void setBusinessJustification(String businessJustification) {
        this.businessJustification = businessJustification;
    }

    public BigDecimal getTotalEstimatedAmount() {
        return totalEstimatedAmount;
    }

    public void setTotalEstimatedAmount(BigDecimal totalEstimatedAmount) {
        this.totalEstimatedAmount = totalEstimatedAmount;
    }

    public RequisitionStatus getStatus() {
        return status;
    }

    public void setStatus(RequisitionStatus status) {
        this.status = status;
    }

    public LocalDateTime getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(LocalDateTime submissionDate) {
        this.submissionDate = submissionDate;
    }

    public String getApproverUser() {
        return approverUser;
    }

    public void setApproverUser(String approverUser) {
        this.approverUser = approverUser;
    }

    public LocalDateTime getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(LocalDateTime approvalDate) {
        this.approvalDate = approvalDate;
    }

    public String getApprovalNotes() {
        return approvalNotes;
    }

    public void setApprovalNotes(String approvalNotes) {
        this.approvalNotes = approvalNotes;
    }

    public List<RequisitionItemDto> getItems() {
        return items;
    }

    public void setItems(List<RequisitionItemDto> items) {
        this.items = items;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
