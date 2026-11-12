package com.zycus.pims.inventory.service;

import com.zycus.pims.common.exception.DuplicateResourceException;
import com.zycus.pims.common.exception.ResourceNotFoundException;
import com.zycus.pims.inventory.dto.ProductDto;
import com.zycus.pims.inventory.dto.ProductRequest;
import com.zycus.pims.inventory.entity.Category;
import com.zycus.pims.inventory.entity.Product;
import com.zycus.pims.inventory.repository.CategoryRepository;
import com.zycus.pims.inventory.repository.InventoryStockRepository;
import com.zycus.pims.inventory.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private InventoryStockRepository inventoryStockRepository;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository, categoryRepository, inventoryStockRepository);
    }

    @Test
    void shouldCreateProductSuccessfully() {
        ProductRequest request = new ProductRequest(
                "SKU-TEST-01", "Test Product", "Description", 1L, "PIECE", new BigDecimal("99.99"), 10
        );
        Category category = new Category("Electronics", "Tech");
        category.setId(1L);

        when(productRepository.existsBySku("SKU-TEST-01")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        Product saved = new Product("SKU-TEST-01", "Test Product", "Description", category, "PIECE", new BigDecimal("99.99"), 10);
        saved.setId(100L);
        when(productRepository.save(any(Product.class))).thenReturn(saved);

        ProductDto result = productService.createProduct(request);

        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getSku()).isEqualTo("SKU-TEST-01");
        assertThat(result.getCategoryName()).isEqualTo("Electronics");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void shouldRejectDuplicateSku() {
        ProductRequest request = new ProductRequest(
                "SKU-DUP", "Dup Product", "Description", 1L, "PIECE", new BigDecimal("50.00"), 5
        );
        when(productRepository.existsBySku("SKU-DUP")).thenReturn(true);

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void shouldThrowWhenCategoryNotFound() {
        ProductRequest request = new ProductRequest(
                "SKU-NO-CAT", "Product", "Description", 999L, "PIECE", new BigDecimal("50.00"), 5
        );
        when(productRepository.existsBySku("SKU-NO-CAT")).thenReturn(false);
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found");
    }
}
