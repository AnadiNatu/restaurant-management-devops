package com.example.demo.adapter.persistence.repository;

import com.example.demo.adapter.persistence.entity.InventoryTransactionEntity;
import com.example.demo.domain.model.enums.InventoryTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransactionEntity , Long> {

    List<InventoryTransactionEntity>
    findByInventoryItemIdOrderByCreatedAtDesc(Long inventoryItemId);

    @Query("""
        SELECT COALESCE(SUM(t.totalCost), 0)
        FROM InventoryTransactionEntity t
        WHERE t.transactionType = :type
          AND t.createdAt >= :from
          AND t.createdAt < :to
    """)
    BigDecimal sumCostByTypeBetween(@Param("type") InventoryTransactionType type, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}