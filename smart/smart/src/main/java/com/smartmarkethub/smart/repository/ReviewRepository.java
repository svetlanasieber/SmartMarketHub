package com.smartmarkethub.smart.repository;

import com.smartmarkethub.smart.model.Review;
import com.smartmarkethub.smart.model.Product;
import com.smartmarkethub.smart.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    Page<Review> findByProduct(Product product, Pageable pageable);
    
    Page<Review> findByUser(User user, Pageable pageable);
    
    boolean existsByUserAndProduct(User user, Product product);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Double getAverageRatingForProduct(@Param("productId") Long productId);
    
    @Query("SELECT r.rating, COUNT(r) FROM Review r " +
           "WHERE r.product.id = :productId GROUP BY r.rating")
    List<Object[]> getRatingDistributionForProduct(@Param("productId") Long productId);
    
    @Query("SELECT r FROM Review r WHERE r.product.id = :productId " +
           "ORDER BY r.createdAt DESC")
    List<Review> findRecentReviewsForProduct(
            @Param("productId") Long productId,
            Pageable pageable);
    
    @Query("SELECT r FROM Review r WHERE r.rating >= :minRating " +
           "ORDER BY r.createdAt DESC")
    List<Review> findPositiveReviews(@Param("minRating") int minRating, Pageable pageable);
}