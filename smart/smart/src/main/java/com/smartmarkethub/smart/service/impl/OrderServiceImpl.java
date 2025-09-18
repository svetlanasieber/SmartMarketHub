package com.smartmarkethub.smart.service.impl;

import com.smartmarkethub.smart.model.Order;
import com.smartmarkethub.smart.model.OrderItem;
import com.smartmarkethub.smart.model.Product;
import com.smartmarkethub.smart.model.User;
import com.smartmarkethub.smart.repository.OrderRepository;
import com.smartmarkethub.smart.repository.ProductRepository;
import com.smartmarkethub.smart.service.OrderService;
import com.smartmarkethub.smart.web.dto.OrderAnalytics;
import com.smartmarkethub.smart.web.dto.OrderSummary;
import com.smartmarkethub.smart.web.exception.InvalidOperationException;
import com.smartmarkethub.smart.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
    
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findAllOrders(String status, LocalDateTime startDate, LocalDateTime endDate) {
        logger.debug("Finding orders with status: {}, startDate: {}, endDate: {}", 
                status, startDate, endDate);
        
        if (status != null && !status.isEmpty()) {
            return orderRepository.findByStatus(status);
        }
        if (startDate != null && endDate != null) {
            return orderRepository.findByDateRange(startDate, endDate);
        }
        return orderRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findByUser(User user) {
        return orderRepository.findByUser(user, Pageable.unpaged()).getContent();
    }

    @Override
    public Order createOrder(User user, List<OrderItem> items) {
        logger.debug("Creating new order for user: {} with {} items", user.getUsername(), items.size());
        
        validateOrder(items);

        Order order = new Order();
        order.setUser(user);
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus("PENDING");

        BigDecimal totalPrice = BigDecimal.ZERO;
        for (OrderItem item : items) {
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", 
                            item.getProduct().getId()));

            if (product.getQuantity() < item.getQuantity()) {
                throw new InvalidOperationException(
                        "Insufficient stock for product: " + product.getName());
            }

            product.setQuantity(product.getQuantity() - item.getQuantity());
            productRepository.save(product);

            item.setPriceAtTime(product.getPrice());
            item.setOrder(order);

            BigDecimal itemTotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
            totalPrice = totalPrice.add(itemTotal);
        }

        order.setTotalAmount(totalPrice);
        order.setOrderItems(new HashSet<>(items));

        Order savedOrder = orderRepository.save(order);
        logger.info("Created order: {} for user: {} with total: {}", 
                savedOrder.getId(), user.getUsername(), totalPrice);
        
        return savedOrder;
    }

    @Override
    public Order updateOrderStatus(Long id, String status) {
        logger.debug("Updating order {} status to: {}", id, status);
        
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        validateStatusTransition(order.getStatus(), status);
        String oldStatus = order.getStatus();
        order.setStatus(status);

        Order updatedOrder = orderRepository.save(order);
        logger.info("Updated order {} status from {} to {}", 
                updatedOrder.getId(), oldStatus, status);
        
        return updatedOrder;
    }

    @Override
    public void cancelOrder(Long id) {
        logger.debug("Cancelling order: {}", id);
        
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        if (!order.getStatus().equals("PENDING")) {
            throw new InvalidOperationException(
                    "Cannot cancel order in status: " + order.getStatus());
        }

    
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity() + item.getQuantity());
            productRepository.save(product);
        }

        order.setStatus("CANCELLED");
        orderRepository.save(order);
        logger.info("Cancelled order: {}", order.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getOrderStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        logger.debug("Generating order statistics from {} to {}", startDate, endDate);
        
        List<Order> orders = orderRepository.findByDateRange(startDate, endDate);
        Map<String, Object> stats = new HashMap<>();
        
     
        stats.put("totalOrders", orders.size());
        BigDecimal totalRevenue = orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalRevenue", totalRevenue);
        
      
        if (!orders.isEmpty()) {
            BigDecimal avgOrderValue = totalRevenue
                    .divide(BigDecimal.valueOf(orders.size()), 2, RoundingMode.HALF_UP);
            stats.put("averageOrderValue", avgOrderValue);
        }
        
    
        Map<String, Long> ordersByStatus = orders.stream()
                .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));
        stats.put("ordersByStatus", ordersByStatus);
        
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderAnalytics getOrderAnalytics(LocalDateTime startDate, LocalDateTime endDate) {
        logger.debug("Generating detailed order analytics from {} to {}", startDate, endDate);
        
        List<Order> orders = orderRepository.findByDateRange(startDate, endDate);
        
      
        BigDecimal totalRevenue = orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
        BigDecimal avgOrderValue = orders.isEmpty() ? BigDecimal.ZERO :
                totalRevenue.divide(BigDecimal.valueOf(orders.size()), 2, RoundingMode.HALF_UP);
                
       
        Map<String, Long> ordersByStatus = orders.stream()
                .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));
                
      
        Map<String, BigDecimal> revenueByCategory = calculateRevenueByCategory(orders);
        
     
        Map<String, Long> ordersByCustomer = orders.stream()
                .collect(Collectors.groupingBy(
                    order -> order.getUser().getUsername(),
                    Collectors.counting()
                ));
                
      
        Map<String, Integer> popularProducts = calculatePopularProducts(orders);
        
     
        Map<String, Double> growthRates = calculateGrowthRates(startDate, endDate);
        
        return OrderAnalytics.builder()
                .totalOrders(orders.size())
                .totalRevenue(totalRevenue)
                .averageOrderValue(avgOrderValue)
                .ordersByStatus(ordersByStatus)
                .revenueByCategory(revenueByCategory)
                .ordersByCustomer(ordersByCustomer)
                .popularProducts(popularProducts)
                .growthRates(growthRates)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderSummary> getRecentOrders(int limit) {
        logger.debug("Getting {} recent orders", limit);
        
        return orderRepository.findAll().stream()
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .limit(limit)
                .map(this::createOrderSummary)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getOrderTrends(String period) {
        logger.debug("Analyzing order trends for period: {}", period);
        
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = calculateStartDate(endDate, period);
        
        List<Order> orders = orderRepository.findByDateRange(startDate, endDate);
        Map<String, Object> trends = new HashMap<>();
        
   
        trends.put("ordersByTime", analyzeOrdersByTime(orders, period));
        trends.put("revenueByTime", analyzeRevenueByTime(orders, period));
        
   
        trends.put("popularTimeSlots", analyzePopularTimeSlots(orders));
        trends.put("categoryTrends", analyzeCategoryTrends(orders));
        
        return trends;
    }

    @Override
    public void validateOrder(List<OrderItem> items) {
        logger.debug("Validating order with {} items", items.size());
        
        if (items == null || items.isEmpty()) {
            throw new InvalidOperationException("Order must contain at least one item");
        }

        for (OrderItem item : items) {
            if (item.getQuantity() <= 0) {
                throw new InvalidOperationException("Item quantity must be greater than zero");
            }

            if (item.getProduct() == null || item.getProduct().getId() == null) {
                throw new InvalidOperationException("Item must have a valid product");
            }

            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", 
                            item.getProduct().getId()));

            if (product.getQuantity() < item.getQuantity()) {
                throw new InvalidOperationException(
                        "Insufficient stock for product: " + product.getName());
            }
        }
    }

    @Override
    public boolean canAccessOrder(Long orderId, Long userId) {
        return orderRepository.findById(orderId)
                .map(order -> order.getUser().getId().equals(userId))
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getRevenueByCategory(LocalDateTime startDate, LocalDateTime endDate) {
        logger.debug("Calculating revenue by category from {} to {}", startDate, endDate);
        
        List<Order> orders = orderRepository.findByDateRange(startDate, endDate);
        Map<String, Object> revenue = new HashMap<>();
        
        Map<String, BigDecimal> revenueByCategory = calculateRevenueByCategory(orders);
        revenue.put("byCategory", revenueByCategory);
        
       
        BigDecimal totalRevenue = revenueByCategory.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
        Map<String, Double> percentagesByCategory = new HashMap<>();
        revenueByCategory.forEach((category, amount) -> {
            double percentage = amount.divide(totalRevenue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
            percentagesByCategory.put(category, percentage);
        });
        revenue.put("percentagesByCategory", percentagesByCategory);
        
        return revenue;
    }

    private void validateStatusTransition(String currentStatus, String newStatus) {
     
        Map<String, List<String>> validTransitions = Map.of(
            "PENDING", List.of("CONFIRMED", "CANCELLED"),
            "CONFIRMED", List.of("SHIPPED", "CANCELLED"),
            "SHIPPED", List.of("DELIVERED"),
            "DELIVERED", List.of(),
            "CANCELLED", List.of()
        );

        if (!validTransitions.containsKey(currentStatus) || 
            !validTransitions.get(currentStatus).contains(newStatus)) {
            throw new InvalidOperationException(
                    "Invalid status transition from " + currentStatus + " to " + newStatus);
        }
    }

    private Map<String, BigDecimal> calculateRevenueByCategory(List<Order> orders) {
        return orders.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .collect(Collectors.groupingBy(
                    item -> item.getProduct().getCategory().getName(),
                    Collectors.reducing(
                        BigDecimal.ZERO,
                        item -> item.getPriceAtTime()
                                .multiply(BigDecimal.valueOf(item.getQuantity())),
                        BigDecimal::add
                    )
                ));
    }

    private Map<String, Integer> calculatePopularProducts(List<Order> orders) {
        return orders.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .collect(Collectors.groupingBy(
                    item -> item.getProduct().getName(),
                    Collectors.summingInt(OrderItem::getQuantity)
                ));
    }

    private Map<String, Double> calculateGrowthRates(LocalDateTime startDate, LocalDateTime endDate) {
    
        LocalDateTime midpoint = startDate.plus(
                ChronoUnit.SECONDS.between(startDate, endDate) / 2, ChronoUnit.SECONDS);
        
    
        List<Order> firstPeriod = orderRepository.findByDateRange(startDate, midpoint);
        List<Order> secondPeriod = orderRepository.findByDateRange(midpoint, endDate);
        
        Map<String, Double> growthRates = new HashMap<>();
        
       
        double orderGrowth = calculateGrowthRate(
                firstPeriod.size(), 
                secondPeriod.size());
        growthRates.put("orders", orderGrowth);
        
     
        BigDecimal firstPeriodRevenue = calculateTotalRevenue(firstPeriod);
        BigDecimal secondPeriodRevenue = calculateTotalRevenue(secondPeriod);
        double revenueGrowth = calculateGrowthRate(
                firstPeriodRevenue.doubleValue(), 
                secondPeriodRevenue.doubleValue());
        growthRates.put("revenue", revenueGrowth);
        
        return growthRates;
    }

    private double calculateGrowthRate(double oldValue, double newValue) {
        if (oldValue == 0) return 100.0; 
        return ((newValue - oldValue) / oldValue) * 100.0;
    }

    private BigDecimal calculateTotalRevenue(List<Order> orders) {
        return orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private OrderSummary createOrderSummary(Order order) {
        return OrderSummary.builder()
                .orderId(order.getId())
                .customerName(order.getUser().getFirstName() + " " + order.getUser().getLastName())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .itemCount(order.getOrderItems().size())
                .orderDate(order.getCreatedAt())
                .productNames(order.getOrderItems().stream()
                        .map(item -> item.getProduct().getName())
                        .collect(Collectors.toList()))
                .build();
    }

    private LocalDateTime calculateStartDate(LocalDateTime endDate, String period) {
        return switch (period.toLowerCase()) {
            case "daily" -> endDate.minusDays(1);
            case "weekly" -> endDate.minusWeeks(1);
            case "monthly" -> endDate.minusMonths(1);
            case "yearly" -> endDate.minusYears(1);
            default -> throw new InvalidOperationException("Invalid period: " + period);
        };
    }

    private Map<String, Long> analyzeOrdersByTime(List<Order> orders, String period) {
        return orders.stream()
                .collect(Collectors.groupingBy(
                    order -> formatDateTime(order.getCreatedAt(), period),
                    Collectors.counting()
                ));
    }

    private Map<String, BigDecimal> analyzeRevenueByTime(List<Order> orders, String period) {
        return orders.stream()
                .collect(Collectors.groupingBy(
                    order -> formatDateTime(order.getCreatedAt(), period),
                    Collectors.mapping(
                        Order::getTotalAmount,
                        Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                    )
                ));
    }

    private Map<Integer, Long> analyzePopularTimeSlots(List<Order> orders) {
        return orders.stream()
                .collect(Collectors.groupingBy(
                    order -> order.getCreatedAt().getHour(),
                    Collectors.counting()
                ));
    }

    private Map<String, List<String>> analyzeCategoryTrends(List<Order> orders) {
        return orders.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .collect(Collectors.groupingBy(
                    item -> item.getProduct().getCategory().getName(),
                    Collectors.mapping(
                        item -> item.getProduct().getName(),
                        Collectors.toList()
                    )
                ));
    }

    private String formatDateTime(LocalDateTime dateTime, String period) {
        return switch (period.toLowerCase()) {
            case "daily" -> dateTime.getHour() + ":00";
            case "weekly" -> dateTime.getDayOfWeek().toString();
            case "monthly" -> dateTime.getDayOfMonth() + "";
            case "yearly" -> dateTime.getMonth().toString();
            default -> dateTime.toString();
        };
    }
}
