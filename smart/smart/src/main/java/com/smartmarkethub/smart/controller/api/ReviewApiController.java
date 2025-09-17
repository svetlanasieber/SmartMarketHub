package com.smartmarkethub.smart.controller.api;

import com.smartmarkethub.smart.model.Review;
import com.smartmarkethub.smart.model.User;
import com.smartmarkethub.smart.service.ReviewService;
import com.smartmarkethub.smart.service.UserService;
import com.smartmarkethub.smart.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewApiController {
    
    private final ReviewService reviewService;
    private final UserService userService;
    private final ProductService productService;

    public ReviewApiController(
            ReviewService reviewService,
            UserService userService,
            ProductService productService) {
        this.reviewService = reviewService;
        this.userService = userService;
        this.productService = productService;
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<Page<Review>> getProductReviews(
            @PathVariable Long productId,
            Pageable pageable) {
        return ResponseEntity.ok(reviewService.findByProduct(
                productService.findById(productId)
                        .orElseThrow(() -> new IllegalArgumentException("Product not found")),
                pageable));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#userId)")
    public ResponseEntity<Page<Review>> getUserReviews(
            @PathVariable Long userId,
            Pageable pageable) {
        return ResponseEntity.ok(reviewService.findByUser(
                userService.findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("User not found")),
                pageable));
    }

    @PostMapping("/product/{productId}")
    public ResponseEntity<Review> createReview(
            @PathVariable Long productId,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody Review review) {
        review.setUser(user);
        review.setProduct(productService.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found")));
        
        Review savedReview = reviewService.createReview(review);
        return ResponseEntity.created(URI.create("/api/reviews/" + savedReview.getId()))
                .body(savedReview);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityService.isReviewAuthor(#id)")
    public ResponseEntity<Review> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody Review review) {
        if (!id.equals(review.getId())) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(reviewService.updateReview(review));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isReviewAuthor(#id)")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        reviewService.deleteReview(id, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/product/{productId}/stats")
    public ResponseEntity<Map<String, Object>> getProductReviewStats(
            @PathVariable Long productId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("averageRating", reviewService.findByProduct(
                productService.findById(productId)
                        .orElseThrow(() -> new IllegalArgumentException("Product not found")),
                Pageable.unpaged()));
        return ResponseEntity.ok(stats);
    }
}