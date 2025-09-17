package com.smartmarkethub.smart.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryCreateRequest {
    @NotBlank(message = "Category name is required")
    private String name;
    
    private String description;
}