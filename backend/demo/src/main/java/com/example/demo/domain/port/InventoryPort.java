package com.example.demo.domain.port;

import com.example.demo.domain.model.InventoryItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InventoryPort {
    InventoryItem save(InventoryItem item);
    Optional<InventoryItem> findById(Long id);
    List<InventoryItem> findAll();
    List<InventoryItem> findLowStock();
    BigDecimal getTotalCostBetween(LocalDateTime from, LocalDateTime to);
    void deleteById(Long id);
}