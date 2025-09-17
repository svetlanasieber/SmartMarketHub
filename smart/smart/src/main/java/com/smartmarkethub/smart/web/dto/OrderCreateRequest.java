package com.smartmarkethub.smart.web.dto;

import com.smartmarkethub.smart.model.OrderItem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class OrderCreateRequest {
    
    @NotEmpty(message = "Order items cannot be empty")
    private List<@Valid OrderItemRequest> items;

    public List<OrderItem> toOrderItems() {
        return items.stream()
                .map(OrderItemRequest::toOrderItem)
                .collect(Collectors.toList());
    }

    @Data
    public static class OrderItemRequest {
        @NotNull(message = "Product ID is required")
        private Long productId;
        
        @NotNull(message = "Quantity is required")
        private Integer quantity;

        public OrderItem toOrderItem() {
            OrderItem item = new OrderItem();
            item.setQuantity(quantity);
            return item;
        }
    }
}


