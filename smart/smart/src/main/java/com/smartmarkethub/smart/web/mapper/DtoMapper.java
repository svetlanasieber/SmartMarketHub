package com.smartmarkethub.smart.web.mapper;

import com.smartmarkethub.smart.model.*;
import com.smartmarkethub.smart.web.dto.*;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class DtoMapper {

    public static ProductResponse toProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getQuantity())
                .category(toCategoryResponse(product.getCategory()))
                .status(determineProductStatus(product))
                .imageUrl(product.getImageUrl())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    public static List<ProductResponse> toProductResponseList(List<Product> products) {
        return products.stream()
                .map(DtoMapper::toProductResponse)
                .collect(Collectors.toList());
    }

    public static Product toProduct(ProductCreateRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        return product;
    }

    public static CategoryResponse toCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .productCount(category.getProducts() != null ? category.getProducts().size() : 0)
                .build();
    }

    public static List<CategoryResponse> toCategoryResponseList(List<Category> categories) {
        return categories.stream()
                .map(DtoMapper::toCategoryResponse)
                .collect(Collectors.toList());
    }

    public static OrderResponse toOrderResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .user(toUserResponse(order.getUser()))
                .items(toOrderItemResponseList(order.getOrderItems()))
                .build();
    }

    public static List<OrderResponse> toOrderResponseList(List<Order> orders) {
        return orders.stream()
                .map(DtoMapper::toOrderResponse)
                .collect(Collectors.toList());
    }

    public static OrderItemResponse toOrderItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .product(toProductResponse(item.getProduct()))
                .quantity(item.getQuantity())
                .priceAtTime(item.getPriceAtTime())
                .build();
    }

    public static List<OrderItemResponse> toOrderItemResponseList(Set<OrderItem> items) {
        return items.stream()
                .map(DtoMapper::toOrderItemResponse)
                .collect(Collectors.toList());
    }

    public static UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .isActive(user.getIsActive())
                .roles(user.getRoles().stream()
                        .map(UserRole::getName)
                        .collect(Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public static List<UserResponse> toUserResponseList(List<User> users) {
        return users.stream()
                .map(DtoMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    private static String determineProductStatus(Product product) {
        if (product.getQuantity() <= 0) {
            return "OUT_OF_STOCK";
        } else if (product.getQuantity() <= 10) {
            return "LOW_STOCK";
        }
        return "IN_STOCK";
    }
}
