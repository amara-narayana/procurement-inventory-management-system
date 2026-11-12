package com.zycus.pims.inventory.controller;

import com.zycus.pims.common.dto.ApiResponse;
import com.zycus.pims.common.dto.GoodsReceiptSyncRequest;
import com.zycus.pims.common.dto.StockAdjustmentDto;
import com.zycus.pims.common.dto.StockMovementDto;
import com.zycus.pims.common.security.SecurityUtils;
import com.zycus.pims.inventory.dto.StockItemResponse;
import com.zycus.pims.inventory.service.InventoryService;
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

import java.util.List;

@RestController
@RequestMapping("/api/inventory/stock")
@Tag(name = "Inventory & Stock", description = "Endpoints for managing stock levels, adjustments, low-stock alerts, and movement audits")
public class StockController {

    private final InventoryService inventoryService;

    public StockController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    @Operation(summary = "Get current stock levels with optional warehouse or product filters")
    public ResponseEntity<ApiResponse<List<StockItemResponse>>> getStock(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long productId) {
        List<StockItemResponse> stock = inventoryService.getStock(warehouseId, productId);
        return ResponseEntity.ok(ApiResponse.success(stock, "Stock levels retrieved successfully"));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get products currently at or below their reorder threshold")
    public ResponseEntity<ApiResponse<List<StockItemResponse>>> getLowStock() {
        List<StockItemResponse> lowStock = inventoryService.getLowStockItems();
        return ResponseEntity.ok(ApiResponse.success(lowStock, "Low-stock products retrieved successfully"));
    }

    @PostMapping("/adjustments")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    @Operation(summary = "Record a manual inventory adjustment with audit trail")
    public ResponseEntity<ApiResponse<StockMovementDto>> adjustStock(@Valid @RequestBody StockAdjustmentDto dto) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        StockMovementDto result = inventoryService.adjustStock(dto, username);
        return new ResponseEntity<>(ApiResponse.success(result, "Stock adjusted successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/movements")
    @Operation(summary = "Get paginated audit history of all inventory stock movements")
    public ResponseEntity<ApiResponse<Page<StockMovementDto>>> getMovements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<StockMovementDto> movements = inventoryService.getStockMovements(pageable);
        return ResponseEntity.ok(ApiResponse.success(movements, "Stock movements retrieved successfully"));
    }

    @PostMapping("/goods-receipt")
    @Operation(summary = "Internal integration endpoint to update stock upon goods receipt from procurement")
    public ResponseEntity<ApiResponse<List<StockMovementDto>>> processGoodsReceipt(@RequestBody GoodsReceiptSyncRequest request) {
        List<StockMovementDto> movements = inventoryService.processGoodsReceiptSync(request);
        return ResponseEntity.ok(ApiResponse.success(movements, "Goods receipt processed in inventory"));
    }
}
