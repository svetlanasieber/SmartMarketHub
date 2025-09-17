package com.smartmarkethub.smart.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CategoryUpdateRequest {
    @NotNull(message = "Category ID is required")
    private Long id;

    @NotBlank(message = "Category name is required")
    private String name;
    
    private String description;
}