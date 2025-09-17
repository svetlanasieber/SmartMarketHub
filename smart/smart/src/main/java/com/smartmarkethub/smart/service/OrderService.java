package com.smartmarkethub.smart.service;

import com.smartmarkethub.smart.model.Order;
import com.smartmarkethub.smart.model.OrderItem;
import com.smartmarkethub.smart.model.User;
import com.smartmarkethub.smart.web.dto.OrderAnalytics;
import com.smartmarkethub.smart.web.dto.OrderSummary;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public interface OrderService {


    List<Order> findAllOrders(String status, LocalDateTime startDate, LocalDateTime endDate);


    Optional<Order> findById(Long id);


    List<Order> findByUser(User user);

 
    Order createOrder(User user, List<OrderItem> items);


    Order updateOrderStatus(Long id, String status);


    void cancelOrder(Long id);

    Map<String, Object> getOrderStatistics(LocalDateTime startDate, LocalDateTime endDate);

 
    OrderAnalytics getOrderAnalytics(LocalDateTime startDate, LocalDateTime endDate);

    List<OrderSummary> getRecentOrders(int limit);

 
    Map<String, Object> getOrderTrends(String period);

  
    void validateOrder(List<OrderItem> items);


    boolean canAccessOrder(Long orderId, Long userId);

    Map<String, Object> getRevenueByCategory(LocalDateTime startDate, LocalDateTime endDate);
}
