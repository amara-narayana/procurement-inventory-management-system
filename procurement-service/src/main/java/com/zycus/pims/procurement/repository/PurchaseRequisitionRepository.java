package com.zycus.pims.procurement.repository;

import com.zycus.pims.common.enums.RequisitionStatus;
import com.zycus.pims.procurement.entity.PurchaseRequisition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseRequisitionRepository extends JpaRepository<PurchaseRequisition, Long> {

    Optional<PurchaseRequisition> findByRequisitionNumber(String requisitionNumber);

    List<PurchaseRequisition> findByStatus(RequisitionStatus status);

    List<PurchaseRequisition> findByRequestingUser(String requestingUser);

    @Query("SELECT r FROM PurchaseRequisition r WHERE " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:user IS NULL OR r.requestingUser = :user) " +
           "ORDER BY r.createdAt DESC")
    Page<PurchaseRequisition> findWithFilters(
            @Param("status") RequisitionStatus status,
            @Param("user") String user,
            Pageable pageable);

    long countByStatus(RequisitionStatus status);
}
