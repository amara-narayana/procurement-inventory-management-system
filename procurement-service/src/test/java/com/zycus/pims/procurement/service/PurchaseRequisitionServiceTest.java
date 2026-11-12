package com.zycus.pims.procurement.service;

import com.zycus.pims.common.enums.RequisitionStatus;
import com.zycus.pims.common.exception.ForbiddenException;
import com.zycus.pims.common.exception.InvalidStateTransitionException;
import com.zycus.pims.procurement.dto.PurchaseRequisitionDto;
import com.zycus.pims.procurement.dto.PurchaseRequisitionRequest;
import com.zycus.pims.procurement.dto.RequisitionApprovalRequest;
import com.zycus.pims.procurement.dto.RequisitionItemRequest;
import com.zycus.pims.procurement.entity.PurchaseRequisition;
import com.zycus.pims.procurement.repository.PurchaseRequisitionRepository;
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
class PurchaseRequisitionServiceTest {

    @Mock
    private PurchaseRequisitionRepository requisitionRepository;

    private PurchaseRequisitionService requisitionService;

    @BeforeEach
    void setUp() {
        requisitionService = new PurchaseRequisitionService(requisitionRepository);
    }

    @Test
    void shouldCreateRequisitionInDraftState() {
        RequisitionItemRequest item = new RequisitionItemRequest(
                1L, "SKU-01", "Laptop", 2, new BigDecimal("1000.00")
        );
        PurchaseRequisitionRequest request = new PurchaseRequisitionRequest(
                "Engineering", "Developer equipment", List.of(item)
        );

        when(requisitionRepository.save(any(PurchaseRequisition.class))).thenAnswer(inv -> {
            PurchaseRequisition r = inv.getArgument(0);
            r.setId(10L);
            return r;
        });

        PurchaseRequisitionDto result = requisitionService.createRequisition(request, "john.doe");

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getStatus()).isEqualTo(RequisitionStatus.DRAFT);
        assertThat(result.getTotalEstimatedAmount()).isEqualByComparingTo("2000.00");
        assertThat(result.getRequestingUser()).isEqualTo("john.doe");
        verify(requisitionRepository).save(any(PurchaseRequisition.class));
    }

    @Test
    void shouldSubmitDraftRequisition() {
        PurchaseRequisition requisition = new PurchaseRequisition();
        requisition.setId(5L);
        requisition.setStatus(RequisitionStatus.DRAFT);
        requisition.setRequestingUser("alice");

        when(requisitionRepository.findById(5L)).thenReturn(Optional.of(requisition));
        when(requisitionRepository.save(any(PurchaseRequisition.class))).thenReturn(requisition);

        PurchaseRequisitionDto result = requisitionService.submitRequisition(5L, "alice");

        assertThat(result.getStatus()).isEqualTo(RequisitionStatus.SUBMITTED);
        assertThat(requisition.getSubmissionDate()).isNotNull();
    }

    @Test
    void shouldPreventUserFromApprovingOwnRequisition() {
        PurchaseRequisition requisition = new PurchaseRequisition();
        requisition.setId(8L);
        requisition.setStatus(RequisitionStatus.SUBMITTED);
        requisition.setRequestingUser("bob.manager");

        when(requisitionRepository.findById(8L)).thenReturn(Optional.of(requisition));

        RequisitionApprovalRequest approval = new RequisitionApprovalRequest("APPROVE", "Looks fine");

        assertThatThrownBy(() -> requisitionService.reviewRequisition(8L, approval, "bob.manager"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Segregation of duties violation");

        verify(requisitionRepository, never()).save(any());
    }

    @Test
    void shouldApproveRequisitionWhenReviewedByDifferentAuthorizedUser() {
        PurchaseRequisition requisition = new PurchaseRequisition();
        requisition.setId(12L);
        requisition.setStatus(RequisitionStatus.SUBMITTED);
        requisition.setRequestingUser("carol.employee");

        when(requisitionRepository.findById(12L)).thenReturn(Optional.of(requisition));
        when(requisitionRepository.save(any(PurchaseRequisition.class))).thenReturn(requisition);

        RequisitionApprovalRequest approval = new RequisitionApprovalRequest("APPROVE", "Approved per budget");
        PurchaseRequisitionDto result = requisitionService.reviewRequisition(12L, approval, "dave.approver");

        assertThat(result.getStatus()).isEqualTo(RequisitionStatus.APPROVED);
        assertThat(requisition.getApproverUser()).isEqualTo("dave.approver");
    }

    @Test
    void shouldRejectInvalidStateTransitionOnSubmit() {
        PurchaseRequisition requisition = new PurchaseRequisition();
        requisition.setId(20L);
        requisition.setStatus(RequisitionStatus.APPROVED);

        when(requisitionRepository.findById(20L)).thenReturn(Optional.of(requisition));

        assertThatThrownBy(() -> requisitionService.submitRequisition(20L, "someone"))
                .isInstanceOf(InvalidStateTransitionException.class);
    }
}
