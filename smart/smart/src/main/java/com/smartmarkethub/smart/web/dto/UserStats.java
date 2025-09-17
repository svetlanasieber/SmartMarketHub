package com.smartmarkethub.smart.web.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class UserStats {
    private Long userId;
    private String username;
    private LocalDateTime memberSince;
    private int totalOrders;
    private BigDecimal totalSpent;
    private BigDecimal averageOrderValue;
    private List<String> favoriteCategories;
    private Map<String, Integer> ordersByStatus;
    private Map<String, BigDecimal> spendingByCategory;
    private LocalDateTime lastOrderDate;
    private LocalDateTime lastLoginDate;
    private int reviewsWritten;
    private double averageRating;
}