package com.smartmarkethub.smart.integration.inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class StockItemDto {
    private UUID id;
    private String productName;
    private Integer quantity;
}

@Data
class CreateStockItemRequest {
    @NotBlank
    private String productName;
    @NotNull
    @Min(0)
    private Integer quantity;
}

@Data
class ReduceStockRequest {
    @NotNull
    @Min(1)
    private Integer quantity;
}


