package com.zycus.pims.procurement.controller;

import com.zycus.pims.common.dto.ApiResponse;
import com.zycus.pims.procurement.dto.ProcurementDashboardStatsDto;
import com.zycus.pims.procurement.service.ProcurementReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/procurement/reports")
@Tag(name = "Procurement Reports", description = "Endpoints for procurement KPI summaries, supplier spend, and order status analytics")
public class ProcurementReportController {

    private final ProcurementReportService reportService;

    public ProcurementReportController(ProcurementReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get procurement dashboard KPIs and analytics metrics")
    public ResponseEntity<ApiResponse<ProcurementDashboardStatsDto>> getDashboardStats() {
        ProcurementDashboardStatsDto stats = reportService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(stats, "Dashboard statistics retrieved successfully"));
    }
}
