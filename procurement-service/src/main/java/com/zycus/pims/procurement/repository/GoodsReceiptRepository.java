package com.zycus.pims.procurement.repository;

import com.zycus.pims.procurement.entity.GoodsReceipt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt, Long> {
    Optional<GoodsReceipt> findByReceiptNumber(String receiptNumber);
    List<GoodsReceipt> findByPurchaseOrderId(Long purchaseOrderId);
    Page<GoodsReceipt> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
