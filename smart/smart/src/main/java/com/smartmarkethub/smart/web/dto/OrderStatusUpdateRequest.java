package com.smartmarkethub.smart.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class OrderStatusUpdateRequest {
    
    @NotBlank(message = "Status is required")
    @Pattern(
        regexp = "^(PENDING|CONFIRMED|SHIPPED|DELIVERED|CANCELLED)$",
        message = "Invalid status. Allowed values: PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED"
    )
    private String status;
}


