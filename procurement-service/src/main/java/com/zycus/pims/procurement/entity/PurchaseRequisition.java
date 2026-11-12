package com.zycus.pims.procurement.entity;

import com.zycus.pims.common.enums.RequisitionStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_requisitions")
public class PurchaseRequisition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "requisition_number", nullable = false, unique = true, length = 50)
    private String requisitionNumber;

    @Column(name = "requesting_user", nullable = false, length = 50)
    private String requestingUser;

    @Column(nullable = false, length = 50)
    private String department;

    @Column(name = "business_justification", nullable = false, columnDefinition = "TEXT")
    private String businessJustification;

    @Column(name = "total_estimated_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalEstimatedAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RequisitionStatus status = RequisitionStatus.DRAFT;

    @Column(name = "submission_date")
    private LocalDateTime submissionDate;

    @Column(name = "approver_user", length = 50)
    private String approverUser;

    @Column(name = "approval_date")
    private LocalDateTime approvalDate;

    @Column(name = "approval_notes", columnDefinition = "TEXT")
    private String approvalNotes;

    @OneToMany(mappedBy = "requisition", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<RequisitionItem> items = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void addItem(RequisitionItem item) {
        items.add(item);
        item.setRequisition(this);
        calculateTotal();
    }

    public void removeItem(RequisitionItem item) {
        items.remove(item);
        item.setRequisition(null);
        calculateTotal();
    }

    public void calculateTotal() {
        this.totalEstimatedAmount = items.stream()
                .map(RequisitionItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public PurchaseRequisition() {
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

    public List<RequisitionItem> getItems() {
        return items;
    }

    public void setItems(List<RequisitionItem> items) {
        this.items = items;
        if (items != null) {
            items.forEach(i -> i.setRequisition(this));
            calculateTotal();
        }
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
