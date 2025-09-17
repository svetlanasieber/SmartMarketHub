package com.smartmarkethub.smart.web.dto;

import lombok.Data;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private Long id;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserResponse user;
    private List<OrderItemResponse> items;

    public static OrderResponse fromOrder(com.smartmarkethub.smart.model.Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .user(UserResponse.fromUser(order.getUser()))
                .items(order.getOrderItems().stream()
                        .map(OrderItemResponse::fromOrderItem)
                        .toList())
                .build();
    }
}