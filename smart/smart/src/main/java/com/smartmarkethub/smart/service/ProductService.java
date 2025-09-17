package com.smartmarkethub.smart.service;

import com.smartmarkethub.smart.web.dto.ProductCreateRequest;
import com.smartmarkethub.smart.web.dto.ProductUpdateRequest;
import com.smartmarkethub.smart.web.exception.InvalidOperationException;
import com.smartmarkethub.smart.web.exception.ResourceNotFoundException;
import com.smartmarkethub.smart.model.Category;
import com.smartmarkethub.smart.model.Product;
import com.smartmarkethub.smart.repository.CategoryRepository;
import com.smartmarkethub.smart.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<Product> findProducts(
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String sortBy) {
        
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));
                    
            if (minPrice != null && maxPrice != null) {
                return productRepository.findByCategoryAndPriceRange(
                        categoryId, minPrice, maxPrice, createPageable(sortBy)).getContent();
            }
            return productRepository.findByCategory(category, createPageable(sortBy)).getContent();
        }
        
        if (minPrice != null && maxPrice != null) {
            return productRepository.findByPriceBetween(minPrice, maxPrice, createPageable(sortBy)).getContent();
        }
        
        return productRepository.findAll(createPageable(sortBy)).getContent();
    }

    @Transactional(readOnly = true)
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    public Product createProduct(ProductCreateRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", 
                        request.getCategoryId()));

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setCategory(category);

        Product savedProduct = productRepository.save(product);
        logger.info("Created new product: {}", savedProduct.getName());
        
        return savedProduct;
    }

    public Product updateProduct(ProductUpdateRequest request) {
        Product existingProduct = productRepository.findById(request.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", 
                        request.getId()));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", 
                        request.getCategoryId()));

        existingProduct.setName(request.getName());
        existingProduct.setDescription(request.getDescription());
        existingProduct.setPrice(request.getPrice());
        existingProduct.setCategory(category);

        Product updatedProduct = productRepository.save(existingProduct);
        logger.info("Updated product: {}", updatedProduct.getName());
        
        return updatedProduct;
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        productRepository.delete(product);
        logger.info("Deleted product: {}", product.getName());
    }

    public Product updateStock(Long id, int quantity) {
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

    public void bulkUpdateStock(List<Long> productIds, int quantity) {
        if (quantity < 0) {
            throw new InvalidOperationException("Quantity cannot be negative");
        }

        List<Product> products = productRepository.findAllById(productIds);
        products.forEach(product -> product.setQuantity(quantity));
        productRepository.saveAll(products);
        logger.info("Bulk updated stock for {} products to {}", products.size(), quantity);
    }

    @Transactional(readOnly = true)
    public List<Product> findLowStockProducts(int threshold) {
        return productRepository.findByQuantityLessThan(threshold);
    }

    private Pageable createPageable(String sortBy) {
        if (sortBy == null) {
            return PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.ASC, "name"));
        }
        
        return switch (sortBy.toLowerCase()) {
            case "price_asc" -> PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.ASC, "price"));
            case "price_desc" -> PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.DESC, "price"));
            case "name_desc" -> PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.DESC, "name"));
            default -> PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.ASC, "name"));
        };
    }
}