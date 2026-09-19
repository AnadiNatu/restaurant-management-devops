package com.example.demo.adapter.persistence;

import com.example.demo.adapter.persistence.entity.OrderEntity;
import com.example.demo.adapter.persistence.entity.OrderItemEntity;
import com.example.demo.adapter.persistence.mapper.OrderMapper;
import com.example.demo.adapter.persistence.repository.OrderRepository;
import com.example.demo.domain.model.Order;
import com.example.demo.domain.model.enums.OrderStatus;
import com.example.demo.domain.port.OrderPort;
import com.example.demo.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderPortAdapter implements OrderPort {


    private final OrderRepository repository;
    private final OrderMapper mapper;

    @Override
    public Order save(Order order) {
        return mapper.toDomain(repository.save(mapper.toEntity(order)));
    }

    @Override
    public Optional<Order> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Order> findAll() {
        return repository.findAllByOrderByCreatedAtDesc().stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Order> findByCustomerId(Long customerId) {
        return repository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        return repository.findByStatusOrderByCreatedAtDesc(status.name()).stream()
                .map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Order> findByHandledBy(String email) {
        return repository.findByHandledByOrderByCreatedAtDesc(email).stream()
                .map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Order> findBetween(LocalDateTime from, LocalDateTime to) {
        return repository.findBetween(from, to).stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public BigDecimal getTotalRevenueBetween(LocalDateTime from, LocalDateTime to) {
        return repository.getTotalRevenueBetween(from, to);
    }

    @Override
    public void deleteById(Long id) {
        if (!repository.existsById(id)) throw new ResourceNotFoundException("Order", "id", id);
        repository.deleteById(id);
    }
}