package com.zycus.pims.procurement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class PurchaseRequisitionRequest {

    @NotBlank(message = "Department is required")
    private String department;

    @NotBlank(message = "Business justification is required")
    private String businessJustification;

    @NotEmpty(message = "Requisition must contain at least one item")
    @Valid
    private List<RequisitionItemRequest> items;

    public PurchaseRequisitionRequest() {
    }

    public PurchaseRequisitionRequest(String department, String businessJustification, List<RequisitionItemRequest> items) {
        this.department = department;
        this.businessJustification = businessJustification;
        this.items = items;
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

    public List<RequisitionItemRequest> getItems() {
        return items;
    }

    public void setItems(List<RequisitionItemRequest> items) {
        this.items = items;
    }
}
