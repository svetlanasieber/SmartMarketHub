package com.smartmarkethub.smart.service;

import com.smartmarkethub.smart.model.Product;
import com.smartmarkethub.smart.web.dto.ProductCreateRequest;
import com.smartmarkethub.smart.web.dto.ProductUpdateRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface for managing products.
 */
public interface IProductService {

    /**
     * Finds products based on various criteria.
     *
     * @param categoryId Optional category ID to filter by
     * @param minPrice Optional minimum price
     * @param maxPrice Optional maximum price
     * @param sortBy Optional sorting criteria
     * @return List of products matching the criteria
     */
    List<Product> findProducts(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, String sortBy);

    /**
     * Finds a product by its ID.
     *
     * @param id The ID of the product to find
     * @return Optional containing the product if found, empty otherwise
     */
    Optional<Product> findById(Long id);

    /**
     * Creates a new product.
     *
     * @param request The product creation request
     * @return The created product
     * @throws ResourceNotFoundException if the specified category is not found
     * @throws InvalidOperationException if the product data is invalid
     */
    Product createProduct(ProductCreateRequest request);

    /**
     * Updates an existing product.
     *
     * @param request The product update request
     * @return The updated product
     * @throws ResourceNotFoundException if the product or category is not found
     * @throws InvalidOperationException if the update data is invalid
     */
    Product updateProduct(ProductUpdateRequest request);

    /**
     * Deletes a product by its ID.
     *
     * @param id The ID of the product to delete
     * @throws ResourceNotFoundException if the product is not found
     * @throws InvalidOperationException if the product cannot be deleted
     */
    void deleteProduct(Long id);

    /**
     * Updates the stock quantity of a product.
     *
     * @param id The ID of the product
     * @param quantity The new quantity
     * @return The updated product
     * @throws ResourceNotFoundException if the product is not found
     * @throws InvalidOperationException if the quantity is invalid
     */
    Product updateStock(Long id, int quantity);

    /**
     * Updates stock quantity for multiple products.
     *
     * @param productIds List of product IDs
     * @param quantity The new quantity for all products
     * @throws ResourceNotFoundException if any product is not found
     * @throws InvalidOperationException if the quantity is invalid
     */
    void bulkUpdateStock(List<Long> productIds, int quantity);

    /**
     * Finds products with stock below the specified threshold.
     *
     * @param threshold The stock threshold
     * @return List of products with low stock
     */
    List<Product> findLowStockProducts(int threshold);

    /**
     * Gets product statistics by category.
     *
     * @return Map containing product statistics
     */
    Map<String, Object> getProductStatistics();

    /**
     * Gets trending products based on recent sales.
     *
     * @param limit Maximum number of products to return
     * @return List of trending products
     */
    List<Product> getTrendingProducts(int limit);

    /**
     * Gets related products for a given product.
     *
     * @param productId The ID of the product
     * @param limit Maximum number of related products to return
     * @return List of related products
     */
    List<Product> getRelatedProducts(Long productId, int limit);
}
