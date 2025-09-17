package com.smartmarkethub.smart.web.dto;

import lombok.Data;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String imageUrl;
    private CategoryResponse category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String status;

    public static ProductResponse fromProduct(com.smartmarkethub.smart.model.Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getQuantity())
                .imageUrl(product.getImageUrl())
                .category(CategoryResponse.fromCategory(product.getCategory()))
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .status(product.getStatus())
                .build();
    }
}