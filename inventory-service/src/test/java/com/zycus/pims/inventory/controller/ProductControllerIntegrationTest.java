package com.zycus.pims.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zycus.pims.inventory.dto.CategoryRequest;
import com.zycus.pims.inventory.dto.ProductRequest;
import com.zycus.pims.inventory.entity.Category;
import com.zycus.pims.inventory.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    private Long categoryId;

    @BeforeEach
    void setUp() {
        Category cat = categoryRepository.findByName("Integration Category")
                .orElseGet(() -> categoryRepository.save(new Category("Integration Category", "Integration tests")));
        categoryId = cat.getId();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldCreateAndRetrieveProduct() throws Exception {
        ProductRequest productReq = new ProductRequest(
                "SKU-INT-999",
                "Integration Test Product",
                "High quality test product",
                categoryId,
                "BOX",
                new BigDecimal("49.99"),
                20
        );

        mockMvc.perform(post("/api/inventory/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sku").value("SKU-INT-999"));

        mockMvc.perform(get("/api/inventory/products")
                        .param("search", "Integration Test Product"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @WithMockUser(username = "employee", roles = {"EMPLOYEE"})
    void shouldForbidEmployeeFromCreatingProduct() throws Exception {
        ProductRequest productReq = new ProductRequest(
                "SKU-EMP-001",
                "Unauthorized Product",
                "Should fail",
                categoryId,
                "PIECE",
                new BigDecimal("10.00"),
                5
        );

        mockMvc.perform(post("/api/inventory/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productReq)))
                .andExpect(status().isForbidden());
    }
}
