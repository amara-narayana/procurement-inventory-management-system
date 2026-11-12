package com.zycus.pims.procurement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class RequisitionApprovalRequest {

    @NotBlank(message = "Decision is required")
    @Pattern(regexp = "^(?i)(APPROVE|REJECT)$", message = "Decision must be either 'APPROVE' or 'REJECT'")
    private String decision;

    private String notes;

    public RequisitionApprovalRequest() {
    }

    public RequisitionApprovalRequest(String decision, String notes) {
        this.decision = decision;
        this.notes = notes;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
