package com.zycus.pims.inventory.service;

import com.zycus.pims.common.dto.StockAdjustmentDto;
import com.zycus.pims.common.dto.StockMovementDto;
import com.zycus.pims.common.enums.StockMovementType;
import com.zycus.pims.common.exception.InsufficientStockException;
import com.zycus.pims.inventory.entity.Category;
import com.zycus.pims.inventory.entity.InventoryStock;
import com.zycus.pims.inventory.entity.Product;
import com.zycus.pims.inventory.entity.StockMovement;
import com.zycus.pims.inventory.entity.Warehouse;
import com.zycus.pims.inventory.repository.InventoryStockRepository;
import com.zycus.pims.inventory.repository.ProductRepository;
import com.zycus.pims.inventory.repository.StockMovementRepository;
import com.zycus.pims.inventory.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryStockRepository inventoryStockRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    private InventoryService inventoryService;

    private Product product;
    private Warehouse warehouse;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryService(
                inventoryStockRepository, stockMovementRepository, productRepository, warehouseRepository
        );

        Category category = new Category("Hardware", "Industrial");
        category.setId(1L);

        product = new Product("SKU-INV-01", "Industrial Drill", "Heavy duty", category, "PIECE", new BigDecimal("150.00"), 5);
        product.setId(10L);

        warehouse = new Warehouse("Main Warehouse", "Building A", "Storage");
        warehouse.setId(20L);
    }

    @Test
    void shouldAdjustStockPositivelyAndCreateAuditMovement() {
        StockAdjustmentDto dto = new StockAdjustmentDto(10L, 20L, 15, "Initial restocking", "REF-001");
        InventoryStock existingStock = new InventoryStock(product, warehouse, 10);

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(20L)).thenReturn(Optional.of(warehouse));
        when(inventoryStockRepository.findByProductIdAndWarehouseId(10L, 20L)).thenReturn(Optional.of(existingStock));

        StockMovement savedMovement = new StockMovement(
                product, warehouse, StockMovementType.ADJUSTMENT_ADD, 15, 10, 25, "REF-001", "Initial restocking", "testuser"
        );
        savedMovement.setId(101L);
        when(stockMovementRepository.save(any(StockMovement.class))).thenReturn(savedMovement);

        StockMovementDto result = inventoryService.adjustStock(dto, "testuser");

        assertThat(result.getId()).isEqualTo(101L);
        assertThat(result.getQuantity()).isEqualTo(15);
        assertThat(result.getNewQuantity()).isEqualTo(25);
        assertThat(existingStock.getQuantity()).isEqualTo(25);
        verify(inventoryStockRepository).save(existingStock);
    }

    @Test
    void shouldRejectAdjustmentResultingInNegativeStock() {
        StockAdjustmentDto dto = new StockAdjustmentDto(10L, 20L, -20, "Cycle count deficit", "REF-002");
        InventoryStock existingStock = new InventoryStock(product, warehouse, 5);

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(20L)).thenReturn(Optional.of(warehouse));
        when(inventoryStockRepository.findByProductIdAndWarehouseId(10L, 20L)).thenReturn(Optional.of(existingStock));

        assertThatThrownBy(() -> inventoryService.adjustStock(dto, "testuser"))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Insufficient stock");

        verify(inventoryStockRepository, never()).save(any());
        verify(stockMovementRepository, never()).save(any());
    }
}
