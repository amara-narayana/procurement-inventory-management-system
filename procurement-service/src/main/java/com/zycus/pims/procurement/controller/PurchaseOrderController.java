package com.zycus.pims.procurement.controller;

import com.zycus.pims.common.dto.ApiResponse;
import com.zycus.pims.common.enums.PurchaseOrderStatus;
import com.zycus.pims.common.security.SecurityUtils;
import com.zycus.pims.procurement.dto.CreatePurchaseOrderRequest;
import com.zycus.pims.procurement.dto.PurchaseOrderDto;
import com.zycus.pims.procurement.service.PurchaseOrderService;
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

import java.util.Map;

@RestController
@RequestMapping("/api/procurement/orders")
@Tag(name = "Purchase Orders", description = "Endpoints for creating, managing, approving, and transmitting purchase orders")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    public PurchaseOrderController(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROCUREMENT_MANAGER', 'PROCUREMENT_OFFICER')")
    @Operation(summary = "Create a new purchase order or convert an approved requisition into a purchase order")
    public ResponseEntity<ApiResponse<PurchaseOrderDto>> createOrder(@Valid @RequestBody CreatePurchaseOrderRequest request) {
        String username = SecurityUtils.getCurrentUsername().orElse("PROCUREMENT_OFFICER");
        PurchaseOrderDto created = purchaseOrderService.createPurchaseOrder(request, username);
        return new ResponseEntity<>(ApiResponse.success(created, "Purchase order created successfully"), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get purchase orders with pagination and filtering")
    public ResponseEntity<ApiResponse<Page<PurchaseOrderDto>>> getOrders(
            @RequestParam(required = false) PurchaseOrderStatus status,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PurchaseOrderDto> orders = purchaseOrderService.getOrders(status, supplierId, pageable);
        return ResponseEntity.ok(ApiResponse.success(orders, "Purchase orders retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get purchase order by ID")
    public ResponseEntity<ApiResponse<PurchaseOrderDto>> getOrderById(@PathVariable Long id) {
        PurchaseOrderDto dto = purchaseOrderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.success(dto, "Purchase order retrieved successfully"));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROCUREMENT_MANAGER')")
    @Operation(summary = "Approve a purchase order")
    public ResponseEntity<ApiResponse<PurchaseOrderDto>> approveOrder(@PathVariable Long id) {
        String username = SecurityUtils.getCurrentUsername().orElse("PROCUREMENT_MANAGER");
        PurchaseOrderDto dto = purchaseOrderService.approveOrder(id, username);
        return ResponseEntity.ok(ApiResponse.success(dto, "Purchase order approved successfully"));
    }

    @PostMapping("/{id}/send")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROCUREMENT_MANAGER', 'PROCUREMENT_OFFICER')")
    @Operation(summary = "Mark purchase order as SENT to vendor")
    public ResponseEntity<ApiResponse<PurchaseOrderDto>> sendOrder(@PathVariable Long id) {
        String username = SecurityUtils.getCurrentUsername().orElse("PROCUREMENT_OFFICER");
        PurchaseOrderDto dto = purchaseOrderService.sendOrder(id, username);
        return ResponseEntity.ok(ApiResponse.success(dto, "Purchase order dispatched to supplier"));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROCUREMENT_MANAGER')")
    @Operation(summary = "Cancel a purchase order")
    public ResponseEntity<ApiResponse<PurchaseOrderDto>> cancelOrder(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String notes = body != null ? body.get("reason") : null;
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        PurchaseOrderDto dto = purchaseOrderService.cancelOrder(id, notes, username);
        return ResponseEntity.ok(ApiResponse.success(dto, "Purchase order cancelled"));
    }
}
