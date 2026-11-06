package com.example.demo.adapter.persistence.repository;

import com.example.demo.adapter.persistence.entity.InventoryItemEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryItemEntity , Long> {
    @Query("SELECT i FROM InventoryItemEntity i WHERE i.quantity <= i.minThreshold")
    List<InventoryItemEntity> findLowStockItems();

    @Query("SELECT COALESCE(SUM(i.quantity * i.costPerUnit), 0) FROM InventoryItemEntity i " +
            "WHERE i.lastRestockedAt BETWEEN :from AND :to")
    BigDecimal getTotalCostBetween(LocalDateTime from, LocalDateTime to);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InventoryItemEntity i WHERE i.id = :id")
    Optional<InventoryItemEntity> findByIdForUpdate(@Param("id") Long id);
}