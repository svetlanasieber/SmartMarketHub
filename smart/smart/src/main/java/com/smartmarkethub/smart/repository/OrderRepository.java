package com.smartmarkethub.smart.repository;

import com.smartmarkethub.smart.model.Order;
import com.smartmarkethub.smart.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    Page<Order> findByUser(User user, Pageable pageable);
    
    List<Order> findByStatus(String status);
    
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT o FROM Order o WHERE o.totalAmount >= :minAmount")
    List<Order> findLargeOrders(@Param("minAmount") BigDecimal minAmount);
    
    @Query("SELECT COUNT(o), SUM(o.totalAmount) FROM Order o " +
           "WHERE o.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('DATE', o.createdAt)")
    List<Object[]> getDailySalesStats(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> getOrderStatusStats();
    
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId " +
           "ORDER BY o.createdAt DESC")
    List<Order> findRecentOrdersByUser(@Param("userId") Long userId, Pageable pageable);
}