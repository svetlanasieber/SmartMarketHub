package com.smartmarkethub.smart.web.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class OrderAnalytics {
    private long totalOrders;
    private BigDecimal totalRevenue;
    private BigDecimal averageOrderValue;
    private Map<String, Long> ordersByStatus;
    private Map<String, BigDecimal> revenueByCategory;
    private Map<String, Long> ordersByCustomer;
    private Map<String, Integer> popularProducts;
    private double conversionRate;
    private Map<String, Double> growthRates;
    private Map<String, BigDecimal> dailyRevenue;
    private Map<String, Object> customerSegmentation;
}
