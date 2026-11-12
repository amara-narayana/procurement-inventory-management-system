package com.zycus.pims.procurement.service;

import com.zycus.pims.common.enums.PurchaseOrderStatus;
import com.zycus.pims.common.enums.RequisitionStatus;
import com.zycus.pims.procurement.dto.ProcurementDashboardStatsDto;
import com.zycus.pims.procurement.entity.PurchaseOrder;
import com.zycus.pims.procurement.repository.PurchaseOrderRepository;
import com.zycus.pims.procurement.repository.PurchaseRequisitionRepository;
import com.zycus.pims.procurement.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProcurementReportService {

    private final SupplierRepository supplierRepository;
    private final PurchaseRequisitionRepository requisitionRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    public ProcurementReportService(
            SupplierRepository supplierRepository,
            PurchaseRequisitionRepository requisitionRepository,
            PurchaseOrderRepository purchaseOrderRepository) {
        this.supplierRepository = supplierRepository;
        this.requisitionRepository = requisitionRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
    }

    @Transactional(readOnly = true)
    public ProcurementDashboardStatsDto getDashboardStats() {
        long totalSuppliers = supplierRepository.count();
        long pendingRequisitions = requisitionRepository.countByStatus(RequisitionStatus.SUBMITTED);
        long openOrders = purchaseOrderRepository.countByStatus(PurchaseOrderStatus.SENT)
                + purchaseOrderRepository.countByStatus(PurchaseOrderStatus.PARTIALLY_RECEIVED);

        Map<String, Long> requisitionsByStatus = new HashMap<>();
        for (RequisitionStatus status : RequisitionStatus.values()) {
            requisitionsByStatus.put(status.name(), requisitionRepository.countByStatus(status));
        }

        Map<String, Long> ordersByStatus = new HashMap<>();
        for (PurchaseOrderStatus status : PurchaseOrderStatus.values()) {
            ordersByStatus.put(status.name(), purchaseOrderRepository.countByStatus(status));
        }

        List<PurchaseOrder> allOrders = purchaseOrderRepository.findAll();
        BigDecimal totalSpend = BigDecimal.ZERO;
        Map<String, BigDecimal> spendBySupplier = new HashMap<>();

        for (PurchaseOrder po : allOrders) {
            if (po.getStatus() != PurchaseOrderStatus.CANCELLED) {
                totalSpend = totalSpend.add(po.getTotalAmount());
                String supplierName = po.getSupplier().getName();
                spendBySupplier.put(
                        supplierName,
                        spendBySupplier.getOrDefault(supplierName, BigDecimal.ZERO).add(po.getTotalAmount())
                );
            }
        }

        return new ProcurementDashboardStatsDto(
                totalSuppliers,
                pendingRequisitions,
                openOrders,
                totalSpend,
                requisitionsByStatus,
                ordersByStatus,
                spendBySupplier
        );
    }
}
