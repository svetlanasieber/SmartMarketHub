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

/**
 * Service interface for managing orders.
 */
public interface OrderService {

    /**
     * Finds all orders with optional filtering.
     *
     * @param status Optional order status filter
     * @param startDate Optional start date filter
     * @param endDate Optional end date filter
     * @return List of orders matching the criteria
     */
    List<Order> findAllOrders(String status, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Finds an order by its ID.
     *
     * @param id The ID of the order to find
     * @return Optional containing the order if found, empty otherwise
     */
    Optional<Order> findById(Long id);

    /**
     * Finds all orders for a specific user.
     *
     * @param user The user whose orders to find
     * @return List of orders for the user
     */
    List<Order> findByUser(User user);

    /**
     * Creates a new order.
     *
     * @param user The user placing the order
     * @param items The items in the order
     * @return The created order
     * @throws InvalidOperationException if the order data is invalid or products are out of stock
     */
    Order createOrder(User user, List<OrderItem> items);

    /**
     * Updates the status of an order.
     *
     * @param id The ID of the order
     * @param status The new status
     * @return The updated order
     * @throws ResourceNotFoundException if the order is not found
     * @throws InvalidOperationException if the status transition is invalid
     */
    Order updateOrderStatus(Long id, String status);

    /**
     * Cancels an order.
     *
     * @param id The ID of the order to cancel
     * @throws ResourceNotFoundException if the order is not found
     * @throws InvalidOperationException if the order cannot be cancelled
     */
    void cancelOrder(Long id);

    /**
     * Gets order statistics for a date range.
     *
     * @param startDate Start of the date range
     * @param endDate End of the date range
     * @return Map containing order statistics
     */
    Map<String, Object> getOrderStatistics(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Gets detailed analytics for orders.
     *
     * @param startDate Start of the date range
     * @param endDate End of the date range
     * @return OrderAnalytics object containing detailed analytics
     */
    OrderAnalytics getOrderAnalytics(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Gets a summary of recent orders.
     *
     * @param limit Maximum number of orders to return
     * @return List of order summaries
     */
    List<OrderSummary> getRecentOrders(int limit);

    /**
     * Gets order trends and patterns.
     *
     * @param period The time period for analysis (e.g., "daily", "weekly", "monthly")
     * @return Map containing trend analysis
     */
    Map<String, Object> getOrderTrends(String period);

    /**
     * Validates an order before processing.
     *
     * @param items The items to validate
     * @throws InvalidOperationException if the order is invalid
     */
    void validateOrder(List<OrderItem> items);

    /**
     * Checks if a user can perform operations on an order.
     *
     * @param orderId The ID of the order
     * @param userId The ID of the user
     * @return true if the user has permission, false otherwise
     */
    boolean canAccessOrder(Long orderId, Long userId);

    /**
     * Gets revenue statistics by category.
     *
     * @param startDate Start of the date range
     * @param endDate End of the date range
     * @return Map containing revenue by category
     */
    Map<String, Object> getRevenueByCategory(LocalDateTime startDate, LocalDateTime endDate);
}