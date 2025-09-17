package com.smartmarkethub.smart.repository;

import com.smartmarkethub.smart.model.Product;
import com.smartmarkethub.smart.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    Page<Product> findByCategory(Category category, Pageable pageable);
    
    Page<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    
    List<Product> findByQuantityLessThan(int quantity);
    
    @Query("SELECT p FROM Product p WHERE p.quantity > 0 ORDER BY p.quantity ASC")
    List<Product> findLowStockProducts(Pageable pageable);
    
    @Query(value = "SELECT * FROM products p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR CAST(p.description AS VARCHAR) LIKE CONCAT('%', LOWER(:keyword), '%')", 
           nativeQuery = true)
    Page<Product> search(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId " +
           "AND p.price BETWEEN :minPrice AND :maxPrice")
    Page<Product> findByCategoryAndPriceRange(
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);
    
    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.category.id = :categoryId")
    boolean existsProductInCategory(@Param("categoryId") Long categoryId);
}