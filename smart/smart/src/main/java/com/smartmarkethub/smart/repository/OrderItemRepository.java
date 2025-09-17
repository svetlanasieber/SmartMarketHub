package com.smartmarkethub.smart.repository;

import com.smartmarkethub.smart.model.OrderItem;
import com.smartmarkethub.smart.model.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    List<OrderItem> findByProduct(Product product);
    
    @Query("SELECT oi.product.id, SUM(oi.quantity) as totalQuantity " +
           "FROM OrderItem oi GROUP BY oi.product.id " +
           "ORDER BY totalQuantity DESC")
    List<Object[]> findMostSoldProducts(Pageable pageable);
    
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.id = :orderId")
    List<OrderItem> findByOrderId(@Param("orderId") Long orderId);
    
    @Query("SELECT oi.product.id, COUNT(oi) as orderCount " +
           "FROM OrderItem oi " +
           "WHERE oi.order.createdAt >= :startDate " +
           "GROUP BY oi.product.id " +
           "HAVING COUNT(oi) >= :minOrders")
    List<Object[]> findPopularProducts(
            @Param("startDate") LocalDateTime startDate,
            @Param("minOrders") long minOrders);
}