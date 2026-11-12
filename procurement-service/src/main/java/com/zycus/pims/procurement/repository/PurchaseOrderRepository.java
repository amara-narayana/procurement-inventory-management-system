package com.zycus.pims.procurement.repository;

import com.zycus.pims.common.enums.PurchaseOrderStatus;
import com.zycus.pims.procurement.entity.PurchaseOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    Optional<PurchaseOrder> findByPoNumber(String poNumber);

    List<PurchaseOrder> findBySupplierId(Long supplierId);

    List<PurchaseOrder> findByStatus(PurchaseOrderStatus status);

    @Query("SELECT po FROM PurchaseOrder po WHERE " +
           "(:status IS NULL OR po.status = :status) AND " +
           "(:supplierId IS NULL OR po.supplier.id = :supplierId) " +
           "ORDER BY po.createdAt DESC")
    Page<PurchaseOrder> findWithFilters(
            @Param("status") PurchaseOrderStatus status,
            @Param("supplierId") Long supplierId,
            Pageable pageable);

    long countByStatus(PurchaseOrderStatus status);
}
