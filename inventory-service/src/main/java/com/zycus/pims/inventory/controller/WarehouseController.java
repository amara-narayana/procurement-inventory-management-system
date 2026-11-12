package com.zycus.pims.inventory.controller;

import com.zycus.pims.common.dto.ApiResponse;
import com.zycus.pims.inventory.dto.WarehouseDto;
import com.zycus.pims.inventory.dto.WarehouseRequest;
import com.zycus.pims.inventory.service.WarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory/warehouses")
@Tag(name = "Warehouses", description = "Endpoints for managing warehouses and fulfillment locations")
public class WarehouseController {

    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @GetMapping
    @Operation(summary = "Get all warehouses")
    public ResponseEntity<ApiResponse<List<WarehouseDto>>> getAllWarehouses() {
        return ResponseEntity.ok(ApiResponse.success(warehouseService.getAllWarehouses(), "Warehouses retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get warehouse by ID")
    public ResponseEntity<ApiResponse<WarehouseDto>> getWarehouseById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(warehouseService.getWarehouseById(id), "Warehouse retrieved successfully"));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    @Operation(summary = "Create a new warehouse")
    public ResponseEntity<ApiResponse<WarehouseDto>> createWarehouse(@Valid @RequestBody WarehouseRequest request) {
        WarehouseDto created = warehouseService.createWarehouse(request);
        return new ResponseEntity<>(ApiResponse.success(created, "Warehouse created successfully"), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    @Operation(summary = "Update an existing warehouse")
    public ResponseEntity<ApiResponse<WarehouseDto>> updateWarehouse(
            @PathVariable Long id,
            @Valid @RequestBody WarehouseRequest request) {
        WarehouseDto updated = warehouseService.updateWarehouse(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Warehouse updated successfully"));
    }
}
