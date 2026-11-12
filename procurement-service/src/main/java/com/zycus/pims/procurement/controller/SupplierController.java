package com.zycus.pims.procurement.controller;

import com.zycus.pims.common.dto.ApiResponse;
import com.zycus.pims.procurement.dto.SupplierDto;
import com.zycus.pims.procurement.dto.SupplierRequest;
import com.zycus.pims.procurement.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/procurement/suppliers")
@Tag(name = "Suppliers", description = "Endpoints for managing vendors and supplier records")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    @Operation(summary = "Get all suppliers")
    public ResponseEntity<ApiResponse<List<SupplierDto>>> getAllSuppliers() {
        return ResponseEntity.ok(ApiResponse.success(supplierService.getAllSuppliers(), "Suppliers retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get supplier by ID")
    public ResponseEntity<ApiResponse<SupplierDto>> getSupplierById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(supplierService.getSupplierById(id), "Supplier retrieved successfully"));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROCUREMENT_MANAGER', 'PROCUREMENT_OFFICER')")
    @Operation(summary = "Create a new supplier")
    public ResponseEntity<ApiResponse<SupplierDto>> createSupplier(@Valid @RequestBody SupplierRequest request) {
        SupplierDto created = supplierService.createSupplier(request);
        return new ResponseEntity<>(ApiResponse.success(created, "Supplier created successfully"), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROCUREMENT_MANAGER', 'PROCUREMENT_OFFICER')")
    @Operation(summary = "Update an existing supplier")
    public ResponseEntity<ApiResponse<SupplierDto>> updateSupplier(
            @PathVariable Long id,
            @Valid @RequestBody SupplierRequest request) {
        SupplierDto updated = supplierService.updateSupplier(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Supplier updated successfully"));
    }
}
