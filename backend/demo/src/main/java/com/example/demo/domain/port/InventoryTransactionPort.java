package com.example.demo.domain.port;

import com.example.demo.domain.model.InventoryTransaction;
import com.example.demo.domain.model.enums.InventoryTransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface InventoryTransactionPort {
    InventoryTransaction save(InventoryTransaction transaction);

    List<InventoryTransaction> findByInventoryItemId(Long inventoryItemId);

    BigDecimal sumCostByTypeBetween(InventoryTransactionType type, LocalDateTime from, LocalDateTime to);
}