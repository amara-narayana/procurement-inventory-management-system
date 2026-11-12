package com.zycus.pims.procurement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zycus.pims.common.enums.SupplierStatus;
import com.zycus.pims.procurement.dto.PurchaseRequisitionRequest;
import com.zycus.pims.procurement.dto.RequisitionItemRequest;
import com.zycus.pims.procurement.dto.SupplierRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProcurementControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "officer", roles = {"PROCUREMENT_OFFICER"})
    void shouldCreateAndListSuppliers() throws Exception {
        SupplierRequest request = new SupplierRequest(
                "Acme Industrial Ltd",
                "Jane Doe",
                "jane.d@acme.com",
                "555-9876",
                "742 Evergreen Terrace",
                "TAX-ACME-001",
                SupplierStatus.ACTIVE
        );

        mockMvc.perform(post("/api/procurement/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Acme Industrial Ltd"));

        mockMvc.perform(get("/api/procurement/suppliers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(username = "alice.engineer", roles = {"EMPLOYEE"})
    void shouldCreateAndSubmitRequisition() throws Exception {
        RequisitionItemRequest item = new RequisitionItemRequest(
                1L, "PROD-001", "Test Product", 5, new BigDecimal("100.00")
        );
        PurchaseRequisitionRequest req = new PurchaseRequisitionRequest(
                "Engineering", "Project Delta resources", List.of(item)
        );

        String responseJson = mockMvc.perform(post("/api/procurement/requisitions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andReturn().getResponse().getContentAsString();

        Number id = com.jayway.jsonpath.JsonPath.read(responseJson, "$.data.id");

        mockMvc.perform(post("/api/procurement/requisitions/" + id + "/submit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"));
    }
}
