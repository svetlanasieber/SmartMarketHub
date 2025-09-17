package com.smartmarkethub.smart.web.controller;

import com.smartmarkethub.smart.model.Product;
import com.smartmarkethub.smart.service.ProductService;
import com.smartmarkethub.smart.web.dto.ProductCreateRequest;
import com.smartmarkethub.smart.web.dto.ProductResponse;
import com.smartmarkethub.smart.web.dto.ProductUpdateRequest;
import com.smartmarkethub.smart.web.exception.InvalidOperationException;
import com.smartmarkethub.smart.web.exception.ResourceNotFoundException;
import com.smartmarkethub.smart.web.mapper.DtoMapper;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String sortBy) {
        
        logger.debug("Fetching products with filters: categoryId={}, minPrice={}, maxPrice={}, sortBy={}", 
                categoryId, minPrice, maxPrice, sortBy);
                
        List<Product> products = productService.findProducts(categoryId, minPrice, maxPrice, sortBy);
        return ResponseEntity.ok(DtoMapper.toProductResponseList(products));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
        logger.debug("Fetching product with id: {}", id);
        
        return productService.findById(id)
                .map(DtoMapper::toProductResponse)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductCreateRequest request) {
        logger.debug("Creating new product: {}", request.getName());
        
        Product savedProduct = productService.createProduct(request);
        ProductResponse response = DtoMapper.toProductResponse(savedProduct);
        
        return ResponseEntity.created(URI.create("/api/products/" + savedProduct.getId()))
                .body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request) {
        
        if (!id.equals(request.getId())) {
            throw new InvalidOperationException("Path variable id does not match request body id");
        }
        
        logger.debug("Updating product with id: {}", id);
        
        Product updatedProduct = productService.updateProduct(request);
        return ResponseEntity.ok(DtoMapper.toProductResponse(updatedProduct));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        logger.debug("Deleting product with id: {}", id);
        
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> updateStock(
            @PathVariable Long id,
            @RequestParam int quantity) {
        
        if (quantity < 0) {
            throw new InvalidOperationException("Quantity cannot be negative");
        }
        
        logger.debug("Updating stock for product id: {} to quantity: {}", id, quantity);
        
        Product updatedProduct = productService.updateStock(id, quantity);
        return ResponseEntity.ok(DtoMapper.toProductResponse(updatedProduct));
    }

    @PutMapping("/bulk-stock-update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> bulkUpdateStock(
            @RequestParam List<Long> productIds,
            @RequestParam int quantity) {
        
        if (quantity < 0) {
            throw new InvalidOperationException("Quantity cannot be negative");
        }
        
        logger.debug("Bulk updating stock for {} products to quantity: {}", productIds.size(), quantity);
        
        productService.bulkUpdateStock(productIds, quantity);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProductResponse>> getLowStockProducts(
            @RequestParam(defaultValue = "10") int threshold) {
        
        logger.debug("Fetching low stock products with threshold: {}", threshold);
        
        List<Product> products = productService.findLowStockProducts(threshold);
        return ResponseEntity.ok(DtoMapper.toProductResponseList(products));
    }
}
