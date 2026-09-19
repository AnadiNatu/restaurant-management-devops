package com.example.demo.domain.port;

import com.example.demo.domain.model.Order;
import com.example.demo.domain.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderPort {
    Order save(Order order);
    Optional<Order> findById(Long id);
    List<Order> findAll();
    List<Order> findByCustomerId(Long customerId);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByHandledBy(String email);
    List<Order> findBetween(LocalDateTime from, LocalDateTime to);
    BigDecimal getTotalRevenueBetween(LocalDateTime from, LocalDateTime to);
    void deleteById(Long id);
}
