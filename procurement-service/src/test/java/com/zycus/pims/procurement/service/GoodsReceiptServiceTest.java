package com.zycus.pims.procurement.service;

import com.zycus.pims.common.enums.PurchaseOrderStatus;
import com.zycus.pims.common.enums.SupplierStatus;
import com.zycus.pims.common.exception.BadRequestException;
import com.zycus.pims.procurement.client.InventoryClient;
import com.zycus.pims.procurement.dto.CreateGoodsReceiptRequest;
import com.zycus.pims.procurement.dto.GoodsReceiptDto;
import com.zycus.pims.procurement.dto.GoodsReceiptItemRequest;
import com.zycus.pims.procurement.entity.GoodsReceipt;
import com.zycus.pims.procurement.entity.PurchaseOrder;
import com.zycus.pims.procurement.entity.PurchaseOrderItem;
import com.zycus.pims.procurement.entity.Supplier;
import com.zycus.pims.procurement.repository.GoodsReceiptRepository;
import com.zycus.pims.procurement.repository.PurchaseOrderItemRepository;
import com.zycus.pims.procurement.repository.PurchaseOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoodsReceiptServiceTest {

    @Mock
    private GoodsReceiptRepository goodsReceiptRepository;

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private PurchaseOrderItemRepository purchaseOrderItemRepository;

    @Mock
    private InventoryClient inventoryClient;

    private GoodsReceiptService goodsReceiptService;

    private PurchaseOrder purchaseOrder;
    private PurchaseOrderItem poItem;

    @BeforeEach
    void setUp() {
        goodsReceiptService = new GoodsReceiptService(
                goodsReceiptRepository, purchaseOrderRepository, purchaseOrderItemRepository, inventoryClient
        );

        Supplier supplier = new Supplier("Test Supplier", "Contact", "test@test.com", "555-0000", "123 St", "TAX-999", SupplierStatus.ACTIVE);
        supplier.setId(1L);

        purchaseOrder = new PurchaseOrder();
        purchaseOrder.setId(10L);
        purchaseOrder.setPoNumber("PO-2026-TEST");
        purchaseOrder.setSupplier(supplier);
        purchaseOrder.setStatus(PurchaseOrderStatus.SENT);

        poItem = new PurchaseOrderItem(101L, "SKU-LAP", "Laptop", 10, new BigDecimal("1200.00"));
        poItem.setId(1L);
        purchaseOrder.addItem(poItem);
    }

    @Test
    void shouldProcessPartialGoodsReceiptAndSetOrderStatus() {
        when(purchaseOrderRepository.findById(10L)).thenReturn(Optional.of(purchaseOrder));
        when(purchaseOrderItemRepository.findById(1L)).thenReturn(Optional.of(poItem));

        GoodsReceiptItemRequest itemReq = new GoodsReceiptItemRequest(1L, 4, "Initial batch of 4");
        CreateGoodsReceiptRequest receiptReq = new CreateGoodsReceiptRequest(1L, "Partial delivery received", List.of(itemReq));

        when(goodsReceiptRepository.save(any(GoodsReceipt.class))).thenAnswer(inv -> {
            GoodsReceipt gr = inv.getArgument(0);
            gr.setId(500L);
            return gr;
        });

        GoodsReceiptDto result = goodsReceiptService.createGoodsReceipt(10L, receiptReq, "inventory.user");

        assertThat(result.getId()).isEqualTo(500L);
        assertThat(poItem.getQuantityReceived()).isEqualTo(4);
        assertThat(purchaseOrder.getStatus()).isEqualTo(PurchaseOrderStatus.PARTIALLY_RECEIVED);
        verify(inventoryClient).syncGoodsReceipt(any());
    }

    @Test
    void shouldProcessFullGoodsReceiptAndMarkOrderReceived() {
        when(purchaseOrderRepository.findById(10L)).thenReturn(Optional.of(purchaseOrder));
        when(purchaseOrderItemRepository.findById(1L)).thenReturn(Optional.of(poItem));

        GoodsReceiptItemRequest itemReq = new GoodsReceiptItemRequest(1L, 10, "Complete delivery of 10");
        CreateGoodsReceiptRequest receiptReq = new CreateGoodsReceiptRequest(1L, "Full delivery", List.of(itemReq));

        when(goodsReceiptRepository.save(any(GoodsReceipt.class))).thenAnswer(inv -> {
            GoodsReceipt gr = inv.getArgument(0);
            gr.setId(501L);
            return gr;
        });

        GoodsReceiptDto result = goodsReceiptService.createGoodsReceipt(10L, receiptReq, "inventory.user");

        assertThat(result.getId()).isEqualTo(501L);
        assertThat(poItem.getQuantityReceived()).isEqualTo(10);
        assertThat(purchaseOrder.getStatus()).isEqualTo(PurchaseOrderStatus.RECEIVED);
    }

    @Test
    void shouldRejectReceiptExceedingOrderedQuantity() {
        when(purchaseOrderRepository.findById(10L)).thenReturn(Optional.of(purchaseOrder));
        when(purchaseOrderItemRepository.findById(1L)).thenReturn(Optional.of(poItem));

        // Ordered is 10, trying to receive 15
        GoodsReceiptItemRequest itemReq = new GoodsReceiptItemRequest(1L, 15, "Too many items");
        CreateGoodsReceiptRequest receiptReq = new CreateGoodsReceiptRequest(1L, "Over shipment", List.of(itemReq));

        assertThatThrownBy(() -> goodsReceiptService.createGoodsReceipt(10L, receiptReq, "user"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("exceeds remaining ordered quantity");
    }
}
