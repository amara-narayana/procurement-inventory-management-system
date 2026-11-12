package com.zycus.pims.procurement.controller;

import com.zycus.pims.common.dto.ApiResponse;
import com.zycus.pims.common.enums.RequisitionStatus;
import com.zycus.pims.common.security.SecurityUtils;
import com.zycus.pims.procurement.dto.PurchaseRequisitionDto;
import com.zycus.pims.procurement.dto.PurchaseRequisitionRequest;
import com.zycus.pims.procurement.dto.RequisitionApprovalRequest;
import com.zycus.pims.procurement.service.PurchaseRequisitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/procurement/requisitions")
@Tag(name = "Purchase Requisitions", description = "Endpoints for creating, managing, submitting, and approving purchase requisitions")
public class PurchaseRequisitionController {

    private final PurchaseRequisitionService requisitionService;

    public PurchaseRequisitionController(PurchaseRequisitionService requisitionService) {
        this.requisitionService = requisitionService;
    }

    @PostMapping
    @Operation(summary = "Create a new purchase requisition (Draft)")
    public ResponseEntity<ApiResponse<PurchaseRequisitionDto>> createRequisition(
            @Valid @RequestBody PurchaseRequisitionRequest request) {
        String username = SecurityUtils.getCurrentUsername().orElse("EMPLOYEE");
        PurchaseRequisitionDto created = requisitionService.createRequisition(request, username);
        return new ResponseEntity<>(ApiResponse.success(created, "Requisition created successfully"), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get purchase requisitions with pagination and filters")
    public ResponseEntity<ApiResponse<Page<PurchaseRequisitionDto>>> getRequisitions(
            @RequestParam(required = false) RequisitionStatus status,
            @RequestParam(required = false) String user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PurchaseRequisitionDto> requisitions = requisitionService.getRequisitions(status, user, pageable);
        return ResponseEntity.ok(ApiResponse.success(requisitions, "Requisitions retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get purchase requisition by ID")
    public ResponseEntity<ApiResponse<PurchaseRequisitionDto>> getRequisitionById(@PathVariable Long id) {
        PurchaseRequisitionDto dto = requisitionService.getRequisitionById(id);
        return ResponseEntity.ok(ApiResponse.success(dto, "Requisition retrieved successfully"));
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "Submit a draft requisition for management approval")
    public ResponseEntity<ApiResponse<PurchaseRequisitionDto>> submitRequisition(@PathVariable Long id) {
        String username = SecurityUtils.getCurrentUsername().orElse("EMPLOYEE");
        PurchaseRequisitionDto dto = requisitionService.submitRequisition(id, username);
        return ResponseEntity.ok(ApiResponse.success(dto, "Requisition submitted for approval"));
    }

    @PostMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('APPROVER', 'PROCUREMENT_MANAGER', 'ADMIN')")
    @Operation(summary = "Approve or reject a submitted purchase requisition")
    public ResponseEntity<ApiResponse<PurchaseRequisitionDto>> reviewRequisition(
            @PathVariable Long id,
            @Valid @RequestBody RequisitionApprovalRequest review) {
        String approver = SecurityUtils.getCurrentUsername().orElse("APPROVER");
        PurchaseRequisitionDto dto = requisitionService.reviewRequisition(id, review, approver);
        String action = "APPROVE".equalsIgnoreCase(review.getDecision()) ? "approved" : "rejected";
        return ResponseEntity.ok(ApiResponse.success(dto, "Requisition " + action + " successfully"));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a purchase requisition")
    public ResponseEntity<ApiResponse<PurchaseRequisitionDto>> cancelRequisition(@PathVariable Long id) {
        String username = SecurityUtils.getCurrentUsername().orElse("EMPLOYEE");
        PurchaseRequisitionDto dto = requisitionService.cancelRequisition(id, username);
        return ResponseEntity.ok(ApiResponse.success(dto, "Requisition cancelled"));
    }
}
