package com.smartmarkethub.smart.web.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderSummary {
    private Long orderId;
    private String customerName;
    private String status;
    private BigDecimal totalAmount;
    private int itemCount;
    private LocalDateTime orderDate;
    private List<String> productNames;
    private String shippingAddress;
    private String paymentMethod;
    private boolean isPaid;
}
