package com.smartmarkethub.smart.service;

import com.smartmarkethub.smart.model.Review;
import com.smartmarkethub.smart.model.Product;
import com.smartmarkethub.smart.model.User;
import com.smartmarkethub.smart.web.exception.InvalidOperationException;
import com.smartmarkethub.smart.web.exception.ResourceNotFoundException;
import com.smartmarkethub.smart.repository.ReviewRepository;
import com.smartmarkethub.smart.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class ReviewService {
    
    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);
    
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    public ReviewService(
            ReviewRepository reviewRepository,
            ProductRepository productRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public Page<Review> findByProduct(Product product, Pageable pageable) {
        return reviewRepository.findByProduct(product, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Review> findByUser(User user, Pageable pageable) {
        return reviewRepository.findByUser(user, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Review> findById(Long id) {
        return reviewRepository.findById(id);
    }

    public Review createReview(Review review) {
        validateReview(review);

        // Check if product exists
        Product product = productRepository.findById(review.getProduct().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", 
                        review.getProduct().getId()));

        // Check if user already reviewed this product
        if (reviewRepository.existsByUserAndProduct(review.getUser(), product)) {
            throw new InvalidOperationException("User already reviewed this product");
        }

        review.setCreatedAt(LocalDateTime.now());
        Review savedReview = reviewRepository.save(review);
        logger.info("Created new review for product: {} by user: {}", 
                product.getName(), review.getUser().getUsername());
        
        return savedReview;
    }

    public Review updateReview(Review review) {
        validateReview(review);

        Review existingReview = reviewRepository.findById(review.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", 
                        review.getId()));

        // Only the review author can update it
        if (!existingReview.getUser().equals(review.getUser())) {
            throw new InvalidOperationException("User not authorized to update this review");
        }

        existingReview.setContent(review.getContent());
        existingReview.setRating(review.getRating());

        Review updatedReview = reviewRepository.save(existingReview);
        logger.info("Updated review: {} for product: {}", 
                updatedReview.getId(), updatedReview.getProduct().getName());
        
        return updatedReview;
    }

    public void deleteReview(Long id, User user) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));

        // Only the review author or an admin can delete it
        if (!review.getUser().equals(user) && 
                !user.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"))) {
            throw new InvalidOperationException("User not authorized to delete this review");
        }

        reviewRepository.delete(review);
        logger.info("Deleted review: {} for product: {}", 
                review.getId(), review.getProduct().getName());
    }

    private void validateReview(Review review) {
        if (review.getContent() == null || review.getContent().trim().isEmpty()) {
            throw new InvalidOperationException("Review content cannot be empty");
        }

        if (review.getRating() < 1 || review.getRating() > 5) {
            throw new InvalidOperationException("Rating must be between 1 and 5");
        }

        if (review.getUser() == null) {
            throw new InvalidOperationException("Review must have a user");
        }

        if (review.getProduct() == null) {
            throw new InvalidOperationException("Review must have a product");
        }
    }
}
