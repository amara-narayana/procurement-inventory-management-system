package com.zycus.pims.procurement.controller;

import com.zycus.pims.common.dto.ApiResponse;
import com.zycus.pims.common.security.SecurityUtils;
import com.zycus.pims.procurement.dto.CreateGoodsReceiptRequest;
import com.zycus.pims.procurement.dto.GoodsReceiptDto;
import com.zycus.pims.procurement.service.GoodsReceiptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/procurement")
@Tag(name = "Goods Receipts", description = "Endpoints for receiving purchased items and updating order fulfillment")
public class GoodsReceiptController {

    private final GoodsReceiptService goodsReceiptService;

    public GoodsReceiptController(GoodsReceiptService goodsReceiptService) {
        this.goodsReceiptService = goodsReceiptService;
    }

    @PostMapping("/orders/{orderId}/receipts")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER', 'PROCUREMENT_OFFICER')")
    @Operation(summary = "Record goods received against an approved/dispatched purchase order")
    public ResponseEntity<ApiResponse<GoodsReceiptDto>> createReceipt(
            @PathVariable Long orderId,
            @Valid @RequestBody CreateGoodsReceiptRequest request) {
        String username = SecurityUtils.getCurrentUsername().orElse("INVENTORY_OFFICER");
        GoodsReceiptDto receipt = goodsReceiptService.createGoodsReceipt(orderId, request, username);
        return new ResponseEntity<>(ApiResponse.success(receipt, "Goods received successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/orders/{orderId}/receipts")
    @Operation(summary = "Get all goods receipts for a specific purchase order")
    public ResponseEntity<ApiResponse<List<GoodsReceiptDto>>> getReceiptsForOrder(@PathVariable Long orderId) {
        List<GoodsReceiptDto> receipts = goodsReceiptService.getReceiptsByPurchaseOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.success(receipts, "Receipts retrieved successfully"));
    }

    @GetMapping("/receipts")
    @Operation(summary = "Get all goods receipts with pagination")
    public ResponseEntity<ApiResponse<Page<GoodsReceiptDto>>> getAllReceipts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<GoodsReceiptDto> receipts = goodsReceiptService.getAllReceipts(pageable);
        return ResponseEntity.ok(ApiResponse.success(receipts, "Receipts retrieved successfully"));
    }

    @GetMapping("/receipts/{id}")
    @Operation(summary = "Get goods receipt by ID")
    public ResponseEntity<ApiResponse<GoodsReceiptDto>> getReceiptById(@PathVariable Long id) {
        GoodsReceiptDto receipt = goodsReceiptService.getReceiptById(id);
        return ResponseEntity.ok(ApiResponse.success(receipt, "Receipt retrieved successfully"));
    }
}
