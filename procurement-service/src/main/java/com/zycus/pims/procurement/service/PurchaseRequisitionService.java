package com.zycus.pims.procurement.service;

import com.zycus.pims.common.enums.RequisitionStatus;
import com.zycus.pims.common.exception.ForbiddenException;
import com.zycus.pims.common.exception.InvalidStateTransitionException;
import com.zycus.pims.common.exception.ResourceNotFoundException;
import com.zycus.pims.procurement.dto.*;
import com.zycus.pims.procurement.entity.PurchaseRequisition;
import com.zycus.pims.procurement.entity.RequisitionItem;
import com.zycus.pims.procurement.repository.PurchaseRequisitionRepository;
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
public class PurchaseRequisitionService {

    private final PurchaseRequisitionRepository requisitionRepository;
    private static final AtomicLong COUNTER = new AtomicLong(System.currentTimeMillis() % 100000);

    public PurchaseRequisitionService(PurchaseRequisitionRepository requisitionRepository) {
        this.requisitionRepository = requisitionRepository;
    }

    @Transactional
    public PurchaseRequisitionDto createRequisition(PurchaseRequisitionRequest request, String username) {
        PurchaseRequisition requisition = new PurchaseRequisition();
        requisition.setRequisitionNumber(generateRequisitionNumber());
        requisition.setRequestingUser(username != null ? username : "EMPLOYEE");
        requisition.setDepartment(request.getDepartment());
        requisition.setBusinessJustification(request.getBusinessJustification());
        requisition.setStatus(RequisitionStatus.DRAFT);

        for (RequisitionItemRequest itemReq : request.getItems()) {
            RequisitionItem item = new RequisitionItem(
                    itemReq.getProductId(),
                    itemReq.getProductSku(),
                    itemReq.getProductName(),
                    itemReq.getQuantity(),
                    itemReq.getEstimatedUnitPrice()
            );
            requisition.addItem(item);
        }

        PurchaseRequisition saved = requisitionRepository.save(requisition);
        return mapToDto(saved);
    }

    @Transactional
    public PurchaseRequisitionDto submitRequisition(Long id, String username) {
        PurchaseRequisition requisition = requisitionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseRequisition", "id", id));

        if (requisition.getStatus() != RequisitionStatus.DRAFT) {
            throw new InvalidStateTransitionException("Purchase Requisition", requisition.getStatus().name(), RequisitionStatus.SUBMITTED.name());
        }

        requisition.setStatus(RequisitionStatus.SUBMITTED);
        requisition.setSubmissionDate(LocalDateTime.now());
        return mapToDto(requisitionRepository.save(requisition));
    }

    @Transactional
    public PurchaseRequisitionDto reviewRequisition(Long id, RequisitionApprovalRequest review, String approverUsername) {
        PurchaseRequisition requisition = requisitionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseRequisition", "id", id));

        if (requisition.getStatus() != RequisitionStatus.SUBMITTED) {
            throw new InvalidStateTransitionException("Only SUBMITTED requisitions can be reviewed. Current status: " + requisition.getStatus());
        }

        // Strict business rule: Users cannot approve their own requisition
        if (approverUsername != null && approverUsername.equalsIgnoreCase(requisition.getRequestingUser())) {
            throw new ForbiddenException("Segregation of duties violation: Users cannot approve their own purchase requisitions.");
        }

        if ("APPROVE".equalsIgnoreCase(review.getDecision())) {
            requisition.setStatus(RequisitionStatus.APPROVED);
        } else {
            requisition.setStatus(RequisitionStatus.REJECTED);
        }

        requisition.setApproverUser(approverUsername);
        requisition.setApprovalDate(LocalDateTime.now());
        requisition.setApprovalNotes(review.getNotes());

        return mapToDto(requisitionRepository.save(requisition));
    }

    @Transactional
    public PurchaseRequisitionDto cancelRequisition(Long id, String username) {
        PurchaseRequisition requisition = requisitionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseRequisition", "id", id));

        if (requisition.getStatus() == RequisitionStatus.CONVERTED_TO_ORDER || requisition.getStatus() == RequisitionStatus.CANCELLED) {
            throw new InvalidStateTransitionException("Cannot cancel requisition in state " + requisition.getStatus());
        }

        requisition.setStatus(RequisitionStatus.CANCELLED);
        return mapToDto(requisitionRepository.save(requisition));
    }

    @Transactional(readOnly = true)
    public Page<PurchaseRequisitionDto> getRequisitions(RequisitionStatus status, String user, Pageable pageable) {
        return requisitionRepository.findWithFilters(status, user, pageable)
                .map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public PurchaseRequisitionDto getRequisitionById(Long id) {
        PurchaseRequisition requisition = requisitionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseRequisition", "id", id));
        return mapToDto(requisition);
    }

    private String generateRequisitionNumber() {
        String year = DateTimeFormatter.ofPattern("yyyy").format(LocalDateTime.now());
        long seq = COUNTER.incrementAndGet();
        return String.format("REQ-%s-%04d", year, seq % 10000);
    }

    public PurchaseRequisitionDto mapToDto(PurchaseRequisition r) {
        List<RequisitionItemDto> items = r.getItems().stream()
                .map(i -> new RequisitionItemDto(
                        i.getId(),
                        i.getProductId(),
                        i.getProductSku(),
                        i.getProductName(),
                        i.getQuantity(),
                        i.getEstimatedUnitPrice(),
                        i.getLineTotal()
                )).collect(Collectors.toList());

        return new PurchaseRequisitionDto(
                r.getId(),
                r.getRequisitionNumber(),
                r.getRequestingUser(),
                r.getDepartment(),
                r.getBusinessJustification(),
                r.getTotalEstimatedAmount(),
                r.getStatus(),
                r.getSubmissionDate(),
                r.getApproverUser(),
                r.getApprovalDate(),
                r.getApprovalNotes(),
                items,
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }
}
