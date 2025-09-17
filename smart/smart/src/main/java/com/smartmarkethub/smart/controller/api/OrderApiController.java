package com.smartmarkethub.smart.controller.api;

import com.smartmarkethub.smart.web.dto.OrderCreateRequest;
import com.smartmarkethub.smart.web.dto.OrderResponse;
import com.smartmarkethub.smart.web.dto.OrderStatusUpdateRequest;
import com.smartmarkethub.smart.model.Order;
import com.smartmarkethub.smart.model.User;
import com.smartmarkethub.smart.service.OrderService;
import com.smartmarkethub.smart.service.UserService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderApiController {
    
    private final OrderService orderService;
    private final UserService userService;

    public OrderApiController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> getAllOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<Order> orders = orderService.findAllOrders(status, startDate, endDate);
        List<OrderResponse> response = orders.stream()
                .map(OrderResponse::fromOrder)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOrderOwner(#id)")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        return orderService.findById(id)
                .map(OrderResponse::fromOrder)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody OrderCreateRequest request) {
        
        Order savedOrder = orderService.createOrder(user, request.toOrderItems());
        OrderResponse response = OrderResponse.fromOrder(savedOrder);
        
        return ResponseEntity.created(URI.create("/api/orders/" + savedOrder.getId()))
                .body(response);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        
        Order updatedOrder = orderService.updateOrderStatus(id, request.getStatus());
        return ResponseEntity.ok(OrderResponse.fromOrder(updatedOrder));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOrderOwner(#id)")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#userId)")
    public ResponseEntity<List<OrderResponse>> getUserOrders(@PathVariable Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
                
        List<Order> orders = orderService.findByUser(user);
        List<OrderResponse> response = orders.stream()
                .map(OrderResponse::fromOrder)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getOrderStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        return ResponseEntity.ok(orderService.getOrderStatistics(startDate, endDate));
    }
}