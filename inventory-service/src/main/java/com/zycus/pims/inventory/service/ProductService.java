package com.zycus.pims.inventory.service;

import com.zycus.pims.common.exception.DuplicateResourceException;
import com.zycus.pims.common.exception.ResourceNotFoundException;
import com.zycus.pims.inventory.dto.ProductDto;
import com.zycus.pims.inventory.dto.ProductRequest;
import com.zycus.pims.inventory.entity.Category;
import com.zycus.pims.inventory.entity.InventoryStock;
import com.zycus.pims.inventory.entity.Product;
import com.zycus.pims.inventory.repository.CategoryRepository;
import com.zycus.pims.inventory.repository.InventoryStockRepository;
import com.zycus.pims.inventory.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryStockRepository inventoryStockRepository;

    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            InventoryStockRepository inventoryStockRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.inventoryStockRepository = inventoryStockRepository;
    }

    @Transactional
    public ProductDto createProduct(ProductRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("Product with SKU '" + request.getSku() + "' already exists");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        Product product = new Product(
                request.getSku(),
                request.getName(),
                request.getDescription(),
                category,
                request.getUnitOfMeasurement(),
                request.getUnitPrice(),
                request.getReorderThreshold()
        );

        Product saved = productRepository.save(product);
        return mapToDto(saved, 0);
    }

    @Transactional
    public ProductDto updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        if (!product.getSku().equalsIgnoreCase(request.getSku()) && productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("Product with SKU '" + request.getSku() + "' already exists");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        product.setSku(request.getSku());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCategory(category);
        product.setUnitOfMeasurement(request.getUnitOfMeasurement());
        product.setUnitPrice(request.getUnitPrice());
        product.setReorderThreshold(request.getReorderThreshold());

        Product saved = productRepository.save(product);
        int totalStock = calculateTotalStock(saved.getId());
        return mapToDto(saved, totalStock);
    }

    @Transactional(readOnly = true)
    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        int totalStock = calculateTotalStock(product.getId());
        return mapToDto(product, totalStock);
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> getProducts(String keyword, Long categoryId, boolean activeOnly, Pageable pageable) {
        return productRepository.findByFilters(keyword, categoryId, activeOnly, pageable)
                .map(p -> mapToDto(p, calculateTotalStock(p.getId())));
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        product.setActive(false);
        productRepository.save(product);
    }

    private int calculateTotalStock(Long productId) {
        List<InventoryStock> stocks = inventoryStockRepository.findByProductId(productId);
        return stocks.stream().mapToInt(InventoryStock::getQuantity).sum();
    }

    private ProductDto mapToDto(Product p, int totalStock) {
        return new ProductDto(
                p.getId(),
                p.getSku(),
                p.getName(),
                p.getDescription(),
                p.getCategory() != null ? p.getCategory().getId() : null,
                p.getCategory() != null ? p.getCategory().getName() : "Uncategorized",
                p.getUnitOfMeasurement(),
                p.getUnitPrice(),
                p.getReorderThreshold(),
                p.isActive(),
                totalStock,
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}
