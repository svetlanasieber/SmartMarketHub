package com.smartmarkethub.smart.service;

import com.smartmarkethub.smart.model.Product;
import com.smartmarkethub.smart.web.dto.ProductCreateRequest;
import com.smartmarkethub.smart.web.dto.ProductUpdateRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public interface IProductService {


    List<Product> findProducts(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, String sortBy);

    Optional<Product> findById(Long id);

    Product createProduct(ProductCreateRequest request);


    Product updateProduct(ProductUpdateRequest request);


    void deleteProduct(Long id);


    Product updateStock(Long id, int quantity);

 
    void bulkUpdateStock(List<Long> productIds, int quantity);


    List<Product> findLowStockProducts(int threshold);

  
    Map<String, Object> getProductStatistics();

    List<Product> getTrendingProducts(int limit);

 
    List<Product> getRelatedProducts(Long productId, int limit);
}
