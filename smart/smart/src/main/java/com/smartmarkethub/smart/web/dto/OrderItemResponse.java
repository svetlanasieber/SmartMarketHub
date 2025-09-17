package com.smartmarkethub.smart.web.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderItemResponse {
    private Long id;
    private ProductResponse product;
    private Integer quantity;
    private BigDecimal priceAtTime;

    public static OrderItemResponse fromOrderItem(com.smartmarkethub.smart.model.OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .product(ProductResponse.fromProduct(item.getProduct()))
                .quantity(item.getQuantity())
                .priceAtTime(item.getPriceAtTime())
                .build();
    }
}