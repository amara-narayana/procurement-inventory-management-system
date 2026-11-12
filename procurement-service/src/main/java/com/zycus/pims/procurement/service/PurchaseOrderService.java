package com.zycus.pims.procurement.service;

import com.zycus.pims.common.enums.PurchaseOrderStatus;
import com.zycus.pims.common.enums.RequisitionStatus;
import com.zycus.pims.common.enums.SupplierStatus;
import com.zycus.pims.common.exception.BadRequestException;
import com.zycus.pims.common.exception.InvalidStateTransitionException;
import com.zycus.pims.common.exception.ResourceNotFoundException;
import com.zycus.pims.procurement.dto.CreatePurchaseOrderRequest;
import com.zycus.pims.procurement.dto.PurchaseOrderDto;
import com.zycus.pims.procurement.dto.PurchaseOrderItemDto;
import com.zycus.pims.procurement.dto.PurchaseOrderItemRequest;
import com.zycus.pims.procurement.entity.PurchaseOrder;
import com.zycus.pims.procurement.entity.PurchaseOrderItem;
import com.zycus.pims.procurement.entity.PurchaseRequisition;
import com.zycus.pims.procurement.entity.Supplier;
import com.zycus.pims.procurement.repository.PurchaseOrderRepository;
import com.zycus.pims.procurement.repository.PurchaseRequisitionRepository;
import com.zycus.pims.procurement.repository.SupplierRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseRequisitionRepository requisitionRepository;
    private final SupplierRepository supplierRepository;
    private static final AtomicLong COUNTER = new AtomicLong(System.currentTimeMillis() % 100000);

    public PurchaseOrderService(
            PurchaseOrderRepository purchaseOrderRepository,
            PurchaseRequisitionRepository requisitionRepository,
            SupplierRepository supplierRepository) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.requisitionRepository = requisitionRepository;
        this.supplierRepository = supplierRepository;
    }

    @Transactional
    public PurchaseOrderDto createPurchaseOrder(CreatePurchaseOrderRequest request, String username) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", request.getSupplierId()));

        if (supplier.getStatus() != SupplierStatus.ACTIVE) {
            throw new BadRequestException("Cannot create purchase order for " + supplier.getStatus() + " supplier");
        }

        PurchaseRequisition requisition = null;
        if (request.getRequisitionId() != null) {
            requisition = requisitionRepository.findById(request.getRequisitionId())
                    .orElseThrow(() -> new ResourceNotFoundException("PurchaseRequisition", "id", request.getRequisitionId()));

            if (requisition.getStatus() != RequisitionStatus.APPROVED) {
                throw new InvalidStateTransitionException("Only APPROVED requisitions can be converted to purchase order. Current status: " + requisition.getStatus());
            }

            requisition.setStatus(RequisitionStatus.CONVERTED_TO_ORDER);
            requisitionRepository.save(requisition);
        }

        PurchaseOrder po = new PurchaseOrder();
        po.setPoNumber(generatePoNumber());
        po.setSupplier(supplier);
        po.setRequisition(requisition);
        po.setExpectedDeliveryDate(request.getExpectedDeliveryDate());
        po.setNotes(request.getNotes());
        po.setCreatedBy(username != null ? username : "PROCUREMENT_OFFICER");
        po.setStatus(PurchaseOrderStatus.DRAFT);

        for (PurchaseOrderItemRequest itemReq : request.getItems()) {
            PurchaseOrderItem item = new PurchaseOrderItem(
                    itemReq.getProductId(),
                    itemReq.getProductSku(),
                    itemReq.getProductName(),
                    itemReq.getQuantityOrdered(),
                    itemReq.getUnitPrice()
            );
            po.addItem(item);
        }

        PurchaseOrder saved = purchaseOrderRepository.save(po);
        return mapToDto(saved);
    }

    @Transactional
    public PurchaseOrderDto approveOrder(Long id, String username) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", "id", id));

        if (po.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new InvalidStateTransitionException("Purchase Order", po.getStatus().name(), PurchaseOrderStatus.APPROVED.name());
        }

        po.setStatus(PurchaseOrderStatus.APPROVED);
        return mapToDto(purchaseOrderRepository.save(po));
    }

    @Transactional
    public PurchaseOrderDto sendOrder(Long id, String username) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", "id", id));

        if (po.getStatus() != PurchaseOrderStatus.APPROVED) {
            throw new InvalidStateTransitionException("Purchase Order must be APPROVED before sending to supplier. Current status: " + po.getStatus());
        }

        po.setStatus(PurchaseOrderStatus.SENT);
        return mapToDto(purchaseOrderRepository.save(po));
    }

    @Transactional
    public PurchaseOrderDto cancelOrder(Long id, String notes, String username) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", "id", id));

        if (po.getStatus() == PurchaseOrderStatus.RECEIVED || po.getStatus() == PurchaseOrderStatus.CANCELLED) {
            throw new InvalidStateTransitionException("Cannot cancel purchase order in status " + po.getStatus());
        }

        po.setStatus(PurchaseOrderStatus.CANCELLED);
        if (notes != null) {
            po.setNotes((po.getNotes() != null ? po.getNotes() + " | " : "") + "Cancelled: " + notes);
        }
        return mapToDto(purchaseOrderRepository.save(po));
    }

    @Transactional(readOnly = true)
    public Page<PurchaseOrderDto> getOrders(PurchaseOrderStatus status, Long supplierId, Pageable pageable) {
        return purchaseOrderRepository.findWithFilters(status, supplierId, pageable)
                .map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public PurchaseOrderDto getOrderById(Long id) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", "id", id));
        return mapToDto(po);
    }

    private String generatePoNumber() {
        String year = DateTimeFormatter.ofPattern("yyyy").format(LocalDateTime.now());
        long seq = COUNTER.incrementAndGet();
        return String.format("PO-%s-%04d", year, seq % 10000);
    }

    public PurchaseOrderDto mapToDto(PurchaseOrder po) {
        List<PurchaseOrderItemDto> items = po.getItems().stream()
                .map(i -> new PurchaseOrderItemDto(
                        i.getId(),
                        i.getProductId(),
                        i.getProductSku(),
                        i.getProductName(),
                        i.getQuantityOrdered(),
                        i.getQuantityReceived(),
                        i.getUnitPrice(),
                        i.getLineTotal()
                )).collect(Collectors.toList());

        return new PurchaseOrderDto(
                po.getId(),
                po.getPoNumber(),
                po.getRequisition() != null ? po.getRequisition().getId() : null,
                po.getRequisition() != null ? po.getRequisition().getRequisitionNumber() : null,
                po.getSupplier().getId(),
                po.getSupplier().getName(),
                po.getTotalAmount(),
                po.getStatus(),
                po.getOrderDate(),
                po.getExpectedDeliveryDate(),
                po.getNotes(),
                po.getCreatedBy(),
                items,
                po.getCreatedAt(),
                po.getUpdatedAt()
        );
    }
}
