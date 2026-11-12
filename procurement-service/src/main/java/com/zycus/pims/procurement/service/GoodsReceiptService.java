package com.zycus.pims.procurement.service;

import com.zycus.pims.common.dto.GoodsReceiptItemSyncDto;
import com.zycus.pims.common.dto.GoodsReceiptSyncRequest;
import com.zycus.pims.common.enums.PurchaseOrderStatus;
import com.zycus.pims.common.exception.BadRequestException;
import com.zycus.pims.common.exception.InvalidStateTransitionException;
import com.zycus.pims.common.exception.ResourceNotFoundException;
import com.zycus.pims.procurement.client.InventoryClient;
import com.zycus.pims.procurement.dto.CreateGoodsReceiptRequest;
import com.zycus.pims.procurement.dto.GoodsReceiptDto;
import com.zycus.pims.procurement.dto.GoodsReceiptItemDto;
import com.zycus.pims.procurement.dto.GoodsReceiptItemRequest;
import com.zycus.pims.procurement.entity.GoodsReceipt;
import com.zycus.pims.procurement.entity.GoodsReceiptItem;
import com.zycus.pims.procurement.entity.PurchaseOrder;
import com.zycus.pims.procurement.entity.PurchaseOrderItem;
import com.zycus.pims.procurement.repository.GoodsReceiptRepository;
import com.zycus.pims.procurement.repository.PurchaseOrderItemRepository;
import com.zycus.pims.procurement.repository.PurchaseOrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class GoodsReceiptService {

    private final GoodsReceiptRepository goodsReceiptRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final InventoryClient inventoryClient;
    private static final AtomicLong COUNTER = new AtomicLong(System.currentTimeMillis() % 100000);

    public GoodsReceiptService(
            GoodsReceiptRepository goodsReceiptRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            PurchaseOrderItemRepository purchaseOrderItemRepository,
            InventoryClient inventoryClient) {
        this.goodsReceiptRepository = goodsReceiptRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderItemRepository = purchaseOrderItemRepository;
        this.inventoryClient = inventoryClient;
    }

    @Transactional
    public GoodsReceiptDto createGoodsReceipt(Long purchaseOrderId, CreateGoodsReceiptRequest request, String username) {
        PurchaseOrder po = purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", "id", purchaseOrderId));

        if (po.getStatus() != PurchaseOrderStatus.SENT && po.getStatus() != PurchaseOrderStatus.PARTIALLY_RECEIVED) {
            throw new InvalidStateTransitionException("Cannot receive goods for purchase order in state: " + po.getStatus() + ". Must be SENT or PARTIALLY_RECEIVED.");
        }

        GoodsReceipt receipt = new GoodsReceipt();
        receipt.setReceiptNumber(generateReceiptNumber());
        receipt.setPurchaseOrder(po);
        receipt.setWarehouseId(request.getWarehouseId());
        receipt.setReceivedBy(username != null ? username : "INVENTORY_OFFICER");
        receipt.setNotes(request.getNotes());
        receipt.setReceivedDate(LocalDateTime.now());

        List<GoodsReceiptItemSyncDto> syncItems = new ArrayList<>();

        for (GoodsReceiptItemRequest itemReq : request.getItems()) {
            PurchaseOrderItem poItem = purchaseOrderItemRepository.findById(itemReq.getPoItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrderItem", "id", itemReq.getPoItemId()));

            if (!poItem.getPurchaseOrder().getId().equals(po.getId())) {
                throw new BadRequestException("Line item ID " + itemReq.getPoItemId() + " does not belong to Purchase Order " + po.getPoNumber());
            }

            int remainingToReceive = poItem.getQuantityOrdered() - poItem.getQuantityReceived();
            if (itemReq.getQuantityReceived() > remainingToReceive) {
                throw new BadRequestException(String.format(
                        "Received quantity (%d) exceeds remaining ordered quantity (%d) for product %s",
                        itemReq.getQuantityReceived(), remainingToReceive, poItem.getProductSku()
                ));
            }

            poItem.setQuantityReceived(poItem.getQuantityReceived() + itemReq.getQuantityReceived());
            purchaseOrderItemRepository.save(poItem);

            GoodsReceiptItem receiptItem = new GoodsReceiptItem(
                    poItem,
                    poItem.getProductId(),
                    poItem.getProductSku(),
                    itemReq.getQuantityReceived(),
                    itemReq.getNotes()
            );
            receipt.addItem(receiptItem);

            syncItems.add(new GoodsReceiptItemSyncDto(
                    poItem.getProductId(),
                    poItem.getProductSku(),
                    itemReq.getQuantityReceived(),
                    poItem.getUnitPrice()
            ));
        }

        // Determine if PO is fully received or partially received
        boolean allFullyReceived = po.getItems().stream()
                .allMatch(item -> item.getQuantityReceived() >= item.getQuantityOrdered());

        po.setStatus(allFullyReceived ? PurchaseOrderStatus.RECEIVED : PurchaseOrderStatus.PARTIALLY_RECEIVED);
        purchaseOrderRepository.save(po);

        GoodsReceipt savedReceipt = goodsReceiptRepository.save(receipt);

        // Sync with Inventory Service
        GoodsReceiptSyncRequest syncRequest = new GoodsReceiptSyncRequest(
                savedReceipt.getReceiptNumber(),
                po.getPoNumber(),
                request.getWarehouseId(),
                syncItems,
                username
        );
        inventoryClient.syncGoodsReceipt(syncRequest);

        return mapToDto(savedReceipt);
    }

    @Transactional(readOnly = true)
    public List<GoodsReceiptDto> getReceiptsByPurchaseOrderId(Long purchaseOrderId) {
        return goodsReceiptRepository.findByPurchaseOrderId(purchaseOrderId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<GoodsReceiptDto> getAllReceipts(Pageable pageable) {
        return goodsReceiptRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public GoodsReceiptDto getReceiptById(Long id) {
        GoodsReceipt receipt = goodsReceiptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GoodsReceipt", "id", id));
        return mapToDto(receipt);
    }

    private String generateReceiptNumber() {
        String year = DateTimeFormatter.ofPattern("yyyy").format(LocalDateTime.now());
        long seq = COUNTER.incrementAndGet();
        return String.format("GRN-%s-%04d", year, seq % 10000);
    }

    private GoodsReceiptDto mapToDto(GoodsReceipt r) {
        List<GoodsReceiptItemDto> items = r.getItems().stream()
                .map(i -> new GoodsReceiptItemDto(
                        i.getId(),
                        i.getPurchaseOrderItem().getId(),
                        i.getProductId(),
                        i.getProductSku(),
                        i.getQuantityReceived(),
                        i.getNotes()
                )).collect(Collectors.toList());

        return new GoodsReceiptDto(
                r.getId(),
                r.getReceiptNumber(),
                r.getPurchaseOrder().getId(),
                r.getPurchaseOrder().getPoNumber(),
                r.getWarehouseId(),
                r.getReceivedDate(),
                r.getReceivedBy(),
                r.getNotes(),
                items,
                r.getCreatedAt()
        );
    }
}
