package com.example.demo.adapter.persistence.mapper;

import com.example.demo.adapter.persistence.entity.InventoryTransactionEntity;
import com.example.demo.domain.model.InventoryTransaction;
import org.springframework.stereotype.Component;

@Component
public class InventoryTransactionMapper {
    public InventoryTransaction toDomain(InventoryTransactionEntity e) {
        if (e == null) return null;

        InventoryTransaction d = new InventoryTransaction();
        d.setId(e.getId());
        d.setInventoryItemId(e.getInventoryItemId());
        d.setTransactionType(e.getTransactionType());
        d.setQuantity(e.getQuantity());
        d.setUnitCost(e.getUnitCost());
        d.setTotalCost(e.getTotalCost());
        d.setQuantityBefore(e.getQuantityBefore());
        d.setQuantityAfter(e.getQuantityAfter());
        d.setReferenceOrderId(e.getReferenceOrderId());
        d.setNotes(e.getNotes());
        d.setCreatedBy(e.getCreatedBy());
        d.setCreatedAt(e.getCreatedAt());

        return d;
    }

    public InventoryTransactionEntity toEntity(InventoryTransaction d) {
        if (d == null) return null;

        return InventoryTransactionEntity.builder()
                .id(d.getId())
                .inventoryItemId(d.getInventoryItemId())
                .transactionType(d.getTransactionType())
                .quantity(d.getQuantity())
                .unitCost(d.getUnitCost())
                .totalCost(d.getTotalCost())
                .quantityBefore(d.getQuantityBefore())
                .quantityAfter(d.getQuantityAfter())
                .referenceOrderId(d.getReferenceOrderId())
                .notes(d.getNotes())
                .createdBy(d.getCreatedBy())
                .createdAt(d.getCreatedAt())
                .build();
    }
}