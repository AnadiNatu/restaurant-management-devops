package com.example.demo.adapter.persistence.repository;

import com.example.demo.adapter.persistence.entity.InventoryItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryItemEntity , Long> {
    @Query("SELECT i FROM InventoryItemEntity i WHERE i.quantity <= i.minThreshold")
    List<InventoryItemEntity> findLowStockItems();

    @Query("SELECT COALESCE(SUM(i.quantity * i.costPerUnit), 0) FROM InventoryItemEntity i " +
            "WHERE i.lastRestockedAt BETWEEN :from AND :to")
    BigDecimal getTotalCostBetween(LocalDateTime from, LocalDateTime to);
}