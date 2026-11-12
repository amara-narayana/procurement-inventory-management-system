package com.zycus.pims.inventory.repository;

import com.zycus.pims.inventory.entity.InventoryStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryStockRepository extends JpaRepository<InventoryStock, Long> {

    Optional<InventoryStock> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

    List<InventoryStock> findByWarehouseId(Long warehouseId);

    List<InventoryStock> findByProductId(Long productId);

    @Query("SELECT s FROM InventoryStock s WHERE s.warehouse.id = :warehouseId AND (:productId IS NULL OR s.product.id = :productId)")
    List<InventoryStock> findByWarehouseAndProduct(
            @Param("warehouseId") Long warehouseId,
            @Param("productId") Long productId);

    @Query("SELECT s FROM InventoryStock s WHERE s.quantity <= s.product.reorderThreshold")
    List<InventoryStock> findLowStockItems();
}
