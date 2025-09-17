package com.smartmarkethub.smart.web.dto;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class CategoryResponse {
    private Long id;
    private String name;
    private String description;
    private int productCount;

    public static CategoryResponse fromCategory(com.smartmarkethub.smart.model.Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .productCount(category.getProducts() != null ? category.getProducts().size() : 0)
                .build();
    }
}