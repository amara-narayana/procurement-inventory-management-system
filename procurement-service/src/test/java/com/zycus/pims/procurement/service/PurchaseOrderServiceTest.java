package com.zycus.pims.procurement.service;

import com.zycus.pims.common.enums.PurchaseOrderStatus;
import com.zycus.pims.common.enums.RequisitionStatus;
import com.zycus.pims.common.enums.SupplierStatus;
import com.zycus.pims.common.exception.InvalidStateTransitionException;
import com.zycus.pims.procurement.dto.CreatePurchaseOrderRequest;
import com.zycus.pims.procurement.dto.PurchaseOrderDto;
import com.zycus.pims.procurement.dto.PurchaseOrderItemRequest;
import com.zycus.pims.procurement.entity.PurchaseOrder;
import com.zycus.pims.procurement.entity.PurchaseOrderItem;
import com.zycus.pims.procurement.entity.PurchaseRequisition;
import com.zycus.pims.procurement.entity.Supplier;
import com.zycus.pims.procurement.repository.PurchaseOrderRepository;
import com.zycus.pims.procurement.repository.PurchaseRequisitionRepository;
import com.zycus.pims.procurement.repository.SupplierRepository;
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
class PurchaseOrderServiceTest {

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private PurchaseRequisitionRepository requisitionRepository;

    @Mock
    private SupplierRepository supplierRepository;

    private PurchaseOrderService purchaseOrderService;

    private Supplier supplier;

    @BeforeEach
    void setUp() {
        purchaseOrderService = new PurchaseOrderService(purchaseOrderRepository, requisitionRepository, supplierRepository);

        supplier = new Supplier("Apex Supplies", "Apex Contact", "apex@example.com", "555-1234", "123 Way", "TAX-12345", SupplierStatus.ACTIVE);
        supplier.setId(1L);
    }

    @Test
    void shouldCreatePurchaseOrderFromApprovedRequisition() {
        PurchaseRequisition req = new PurchaseRequisition();
        req.setId(10L);
        req.setStatus(RequisitionStatus.APPROVED);

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(requisitionRepository.findById(10L)).thenReturn(Optional.of(req));

        PurchaseOrderItemRequest item = new PurchaseOrderItemRequest(100L, "SKU-MON", "Monitor", 5, new BigDecimal("200.00"));
        CreatePurchaseOrderRequest orderRequest = new CreatePurchaseOrderRequest(10L, 1L, null, "PO for monitors", List.of(item));

        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(inv -> {
            PurchaseOrder po = inv.getArgument(0);
            po.setId(50L);
            return po;
        });

        PurchaseOrderDto dto = purchaseOrderService.createPurchaseOrder(orderRequest, "proc.officer");

        assertThat(dto.getId()).isEqualTo(50L);
        assertThat(dto.getStatus()).isEqualTo(PurchaseOrderStatus.DRAFT);
        assertThat(dto.getTotalAmount()).isEqualByComparingTo("1000.00");
        assertThat(req.getStatus()).isEqualTo(RequisitionStatus.CONVERTED_TO_ORDER);
        verify(requisitionRepository).save(req);
    }

    @Test
    void shouldFollowValidStatusProgression() {
        PurchaseOrder po = new PurchaseOrder();
        po.setId(99L);
        po.setSupplier(supplier);
        po.setStatus(PurchaseOrderStatus.DRAFT);

        when(purchaseOrderRepository.findById(99L)).thenReturn(Optional.of(po));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(po);

        PurchaseOrderDto approved = purchaseOrderService.approveOrder(99L, "manager");
        assertThat(approved.getStatus()).isEqualTo(PurchaseOrderStatus.APPROVED);

        PurchaseOrderDto sent = purchaseOrderService.sendOrder(99L, "officer");
        assertThat(sent.getStatus()).isEqualTo(PurchaseOrderStatus.SENT);
    }

    @Test
    void shouldRejectInvalidStatusProgression() {
        PurchaseOrder po = new PurchaseOrder();
        po.setId(88L);
        po.setSupplier(supplier);
        po.setStatus(PurchaseOrderStatus.DRAFT); // not yet approved

        when(purchaseOrderRepository.findById(88L)).thenReturn(Optional.of(po));

        // Attempt to send unapproved order should fail
        assertThatThrownBy(() -> purchaseOrderService.sendOrder(88L, "officer"))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("must be APPROVED before sending");
    }
}
