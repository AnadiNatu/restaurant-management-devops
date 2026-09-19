package com.example.demo.adapter.persistence.repository;

import com.example.demo.adapter.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity , Long>{
    List<OrderEntity> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    List<OrderEntity> findByStatusOrderByCreatedAtDesc(String status);
    List<OrderEntity> findByHandledByOrderByCreatedAtDesc(String handledBy);
    List<OrderEntity> findAllByOrderByCreatedAtDesc();

    @Query("SELECT o FROM OrderEntity o WHERE o.createdAt BETWEEN :from AND :to ORDER BY o.createdAt DESC")
    List<OrderEntity> findBetween(LocalDateTime from, LocalDateTime to);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM OrderEntity o WHERE o.createdAt BETWEEN :from AND :to AND o.status != 'CANCELLED'")
    BigDecimal getTotalRevenueBetween(LocalDateTime from, LocalDateTime to);
}
