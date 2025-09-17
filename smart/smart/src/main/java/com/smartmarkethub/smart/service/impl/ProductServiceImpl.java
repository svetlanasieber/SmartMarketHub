package com.smartmarkethub.smart.service.impl;

import com.smartmarkethub.smart.model.Category;
import com.smartmarkethub.smart.model.Product;
import com.smartmarkethub.smart.repository.CategoryRepository;
import com.smartmarkethub.smart.repository.ProductRepository;
import com.smartmarkethub.smart.service.IProductService;
import com.smartmarkethub.smart.web.dto.ProductCreateRequest;
import com.smartmarkethub.smart.web.dto.ProductUpdateRequest;
import com.smartmarkethub.smart.web.exception.InvalidOperationException;
import com.smartmarkethub.smart.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductServiceImpl implements IProductService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);
    
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductServiceImpl(
            ProductRepository productRepository,
            CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findProducts(
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String sortBy) {
        
        logger.debug("Finding products with filters: categoryId={}, minPrice={}, maxPrice={}, sortBy={}", 
                categoryId, minPrice, maxPrice, sortBy);
        
        Sort sort = createSort(sortBy);
        Pageable pageable = sort != null ? PageRequest.of(0, Integer.MAX_VALUE, sort) : Pageable.unpaged();
        
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));
                    
            if (minPrice != null && maxPrice != null) {
                return productRepository.findByCategoryAndPriceRange(
                        categoryId, minPrice, maxPrice, pageable).getContent();
            }
            return productRepository.findByCategory(category, pageable).getContent();
        }
        
        if (minPrice != null && maxPrice != null) {
            return productRepository.findByPriceBetween(minPrice, maxPrice, pageable).getContent();
        }
        
        return productRepository.findAll(pageable).getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public Product createProduct(ProductCreateRequest request) {
        logger.debug("Creating new product: {}", request.getName());
        
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        validateProductData(request.getName(), request.getPrice(), request.getQuantity());

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setCategory(category);

        Product savedProduct = productRepository.save(product);
        logger.info("Created new product: {} in category: {}", 
                savedProduct.getName(), category.getName());
        
        return savedProduct;
    }

    @Override
    public Product updateProduct(ProductUpdateRequest request) {
        logger.debug("Updating product with id: {}", request.getId());
        
        Product existingProduct = productRepository.findById(request.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getId()));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        validateProductData(request.getName(), request.getPrice(), null);

        existingProduct.setName(request.getName());
        existingProduct.setDescription(request.getDescription());
        existingProduct.setPrice(request.getPrice());
        existingProduct.setCategory(category);

        Product updatedProduct = productRepository.save(existingProduct);
        logger.info("Updated product: {}", updatedProduct.getName());
        
        return updatedProduct;
    }

    @Override
    public void deleteProduct(Long id) {
        logger.debug("Deleting product with id: {}", id);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        productRepository.delete(product);
        logger.info("Deleted product: {}", product.getName());
    }

    @Override
    public Product updateStock(Long id, int quantity) {
        logger.debug("Updating stock for product id: {} to quantity: {}", id, quantity);
        
        if (quantity < 0) {
            throw new InvalidOperationException("Quantity cannot be negative");
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        product.setQuantity(quantity);
        Product updatedProduct = productRepository.save(product);
        logger.info("Updated stock for product: {} to {}", updatedProduct.getName(), quantity);
        
        return updatedProduct;
    }

    @Override
    public void bulkUpdateStock(List<Long> productIds, int quantity) {
        logger.debug("Bulk updating stock for {} products to quantity: {}", productIds.size(), quantity);
        
        if (quantity < 0) {
            throw new InvalidOperationException("Quantity cannot be negative");
        }

        List<Product> products = productRepository.findAllById(productIds);
        if (products.size() != productIds.size()) {
            throw new ResourceNotFoundException("Products", "ids", productIds.toString());
        }

        products.forEach(product -> product.setQuantity(quantity));
        productRepository.saveAll(products);
        logger.info("Bulk updated stock for {} products to {}", products.size(), quantity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findLowStockProducts(int threshold) {
        logger.debug("Finding products with stock below: {}", threshold);
        return productRepository.findByQuantityLessThan(threshold);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getProductStatistics() {
        logger.debug("Generating product statistics");
        
        List<Product> products = productRepository.findAll();
        Map<String, Object> stats = new HashMap<>();
        
        // Total products
        stats.put("totalProducts", products.size());
        
        // Products by category
        Map<String, Long> productsByCategory = products.stream()
                .collect(Collectors.groupingBy(
                    product -> product.getCategory().getName(),
                    Collectors.counting()
                ));
        stats.put("productsByCategory", productsByCategory);
        
        // Stock status
        long outOfStock = products.stream().filter(p -> p.getQuantity() == 0).count();
        long lowStock = products.stream().filter(p -> p.getQuantity() > 0 && p.getQuantity() <= 10).count();
        long inStock = products.stream().filter(p -> p.getQuantity() > 10).count();
        
        Map<String, Long> stockStatus = new HashMap<>();
        stockStatus.put("OUT_OF_STOCK", outOfStock);
        stockStatus.put("LOW_STOCK", lowStock);
        stockStatus.put("IN_STOCK", inStock);
        stats.put("stockStatus", stockStatus);
        
        // Price ranges
        DoubleSummaryStatistics priceStats = products.stream()
                .map(Product::getPrice)
                .mapToDouble(BigDecimal::doubleValue)
                .summaryStatistics();
        
        Map<String, Double> priceRanges = new HashMap<>();
        priceRanges.put("min", priceStats.getMin());
        priceRanges.put("max", priceStats.getMax());
        priceRanges.put("average", priceStats.getAverage());
        stats.put("priceRanges", priceRanges);
        
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getTrendingProducts(int limit) {
        // This would typically use sales data or view counts
        // For now, return products with highest stock turnover
        return productRepository.findAll(Sort.by(Sort.Direction.DESC, "quantity"))
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getRelatedProducts(Long productId, int limit) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
                
        // For now, return products in the same category
        return productRepository.findByCategory(product.getCategory(), Pageable.ofSize(limit + 1))
                .stream()
                .filter(p -> !p.getId().equals(productId))
                .limit(limit)
                .collect(Collectors.toList());
    }

    private Sort createSort(String sortBy) {
        if (sortBy == null) {
            return Sort.by(Sort.Direction.ASC, "name");
        }
        
        return switch (sortBy.toLowerCase()) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "price");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price");
            case "name_desc" -> Sort.by(Sort.Direction.DESC, "name");
            default -> Sort.by(Sort.Direction.ASC, "name");
        };
    }

    private void validateProductData(String name, BigDecimal price, Integer quantity) {
        List<String> errors = new ArrayList<>();
        
        if (name == null || name.trim().isEmpty()) {
            errors.add("Product name is required");
        }
        
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Price must be greater than zero");
        }
        
        if (quantity != null && quantity < 0) {
            errors.add("Quantity cannot be negative");
        }
        
        if (!errors.isEmpty()) {
            throw new InvalidOperationException("Invalid product data: " + String.join(", ", errors));
        }
    }
}
