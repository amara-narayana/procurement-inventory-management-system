package com.zycus.pims.inventory.service;

import com.zycus.pims.common.dto.GoodsReceiptItemSyncDto;
import com.zycus.pims.common.dto.GoodsReceiptSyncRequest;
import com.zycus.pims.common.dto.StockAdjustmentDto;
import com.zycus.pims.common.dto.StockMovementDto;
import com.zycus.pims.common.enums.StockMovementType;
import com.zycus.pims.common.exception.InsufficientStockException;
import com.zycus.pims.common.exception.ResourceNotFoundException;
import com.zycus.pims.inventory.dto.StockItemResponse;
import com.zycus.pims.inventory.entity.InventoryStock;
import com.zycus.pims.inventory.entity.Product;
import com.zycus.pims.inventory.entity.StockMovement;
import com.zycus.pims.inventory.entity.Warehouse;
import com.zycus.pims.inventory.repository.InventoryStockRepository;
import com.zycus.pims.inventory.repository.ProductRepository;
import com.zycus.pims.inventory.repository.StockMovementRepository;
import com.zycus.pims.inventory.repository.WarehouseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private final InventoryStockRepository inventoryStockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    public InventoryService(
            InventoryStockRepository inventoryStockRepository,
            StockMovementRepository stockMovementRepository,
            ProductRepository productRepository,
            WarehouseRepository warehouseRepository) {
        this.inventoryStockRepository = inventoryStockRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @Transactional(readOnly = true)
    public List<StockItemResponse> getStock(Long warehouseId, Long productId) {
        List<InventoryStock> stocks;
        if (warehouseId != null) {
            stocks = inventoryStockRepository.findByWarehouseAndProduct(warehouseId, productId);
        } else if (productId != null) {
            stocks = inventoryStockRepository.findByProductId(productId);
        } else {
            stocks = inventoryStockRepository.findAll();
        }

        return stocks.stream()
                .map(this::mapToStockResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StockItemResponse> getLowStockItems() {
        return inventoryStockRepository.findLowStockItems().stream()
                .map(this::mapToStockResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public StockMovementDto adjustStock(StockAdjustmentDto dto, String username) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", dto.getProductId()));

        Warehouse warehouse = warehouseRepository.findById(dto.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", dto.getWarehouseId()));

        InventoryStock stock = inventoryStockRepository.findByProductIdAndWarehouseId(product.getId(), warehouse.getId())
                .orElseGet(() -> new InventoryStock(product, warehouse, 0));

        int previousQty = stock.getQuantity();
        int newQty = previousQty + dto.getQuantityChange();

        if (newQty < 0) {
            throw new InsufficientStockException(product.getId(), Math.abs(dto.getQuantityChange()), previousQty);
        }

        stock.setQuantity(newQty);
        inventoryStockRepository.save(stock);

        StockMovementType movementType = dto.getQuantityChange() >= 0
                ? StockMovementType.ADJUSTMENT_ADD
                : StockMovementType.ADJUSTMENT_SUBTRACT;

        StockMovement movement = new StockMovement(
                product,
                warehouse,
                movementType,
                Math.abs(dto.getQuantityChange()),
                previousQty,
                newQty,
                dto.getReferenceNumber(),
                dto.getReason(),
                username != null ? username : "SYSTEM"
        );

        StockMovement savedMovement = stockMovementRepository.save(movement);
        return mapToMovementDto(savedMovement);
    }

    @Transactional
    public List<StockMovementDto> processGoodsReceiptSync(GoodsReceiptSyncRequest request) {
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", request.getWarehouseId()));

        List<StockMovementDto> movementDtos = new ArrayList<>();

        for (GoodsReceiptItemSyncDto item : request.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", item.getProductId()));

            InventoryStock stock = inventoryStockRepository.findByProductIdAndWarehouseId(product.getId(), warehouse.getId())
                    .orElseGet(() -> new InventoryStock(product, warehouse, 0));

            int previousQty = stock.getQuantity();
            int newQty = previousQty + item.getQuantityReceived();

            stock.setQuantity(newQty);
            inventoryStockRepository.save(stock);

            StockMovement movement = new StockMovement(
                    product,
                    warehouse,
                    StockMovementType.INBOUND_RECEIPT,
                    item.getQuantityReceived(),
                    previousQty,
                    newQty,
                    request.getReceiptNumber(),
                    "Goods Receipt against PO " + request.getPurchaseOrderNumber(),
                    request.getReceivedBy() != null ? request.getReceivedBy() : "PROCUREMENT_SERVICE"
            );

            StockMovement savedMovement = stockMovementRepository.save(movement);
            movementDtos.add(mapToMovementDto(savedMovement));
        }

        return movementDtos;
    }

    @Transactional(readOnly = true)
    public Page<StockMovementDto> getStockMovements(Pageable pageable) {
        return stockMovementRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::mapToMovementDto);
    }

    private StockItemResponse mapToStockResponse(InventoryStock stock) {
        Product p = stock.getProduct();
        Warehouse w = stock.getWarehouse();
        BigDecimal totalValue = p.getUnitPrice().multiply(BigDecimal.valueOf(stock.getQuantity()));
        boolean isLow = stock.getQuantity() <= p.getReorderThreshold();

        return new StockItemResponse(
                stock.getId(),
                p.getId(),
                p.getSku(),
                p.getName(),
                p.getCategory() != null ? p.getCategory().getName() : "Uncategorized",
                p.getUnitOfMeasurement(),
                p.getUnitPrice(),
                w.getId(),
                w.getName(),
                w.getLocation(),
                stock.getQuantity(),
                totalValue,
                isLow,
                stock.getUpdatedAt()
        );
    }

    private StockMovementDto mapToMovementDto(StockMovement m) {
        StockMovementDto dto = new StockMovementDto();
        dto.setId(m.getId());
        dto.setProductId(m.getProduct().getId());
        dto.setProductSku(m.getProduct().getSku());
        dto.setProductName(m.getProduct().getName());
        dto.setWarehouseId(m.getWarehouse().getId());
        dto.setWarehouseName(m.getWarehouse().getName());
        dto.setMovementType(m.getMovementType());
        dto.setQuantity(m.getQuantity());
        dto.setPreviousQuantity(m.getPreviousQuantity());
        dto.setNewQuantity(m.getNewQuantity());
        dto.setReferenceNumber(m.getReferenceNumber());
        dto.setReason(m.getReason());
        dto.setCreatedBy(m.getCreatedBy());
        dto.setCreatedAt(m.getCreatedAt());
        return dto;
    }
}
