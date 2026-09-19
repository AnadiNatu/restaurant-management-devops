package com.example.demo.adapter.persistence;

import com.example.demo.adapter.persistence.mapper.InventoryMapper;
import com.example.demo.adapter.persistence.repository.InventoryRepository;
import com.example.demo.domain.model.InventoryItem;
import com.example.demo.domain.port.InventoryPort;
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
public class InventoryPortAdapter implements InventoryPort {

    private final InventoryRepository repository;
    private final InventoryMapper mapper;

    @Override
    public InventoryItem save(InventoryItem item) {
        return mapper.toDomain(repository.save(mapper.toEntity(item)));
    }

    @Override
    public Optional<InventoryItem> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<InventoryItem> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<InventoryItem> findLowStock() {
        return repository.findLowStockItems().stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public BigDecimal getTotalCostBetween(LocalDateTime from, LocalDateTime to) {
        return repository.getTotalCostBetween(from, to);
    }

    @Override
    public void deleteById(Long id) {
        if (!repository.existsById(id)) throw new ResourceNotFoundException("InventoryItem", "id", id);
        repository.deleteById(id);
    }

}
