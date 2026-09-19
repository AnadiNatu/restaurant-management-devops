package com.example.demo.adapter.persistence.mapper;

import com.example.demo.adapter.persistence.entity.InventoryItemEntity;
import com.example.demo.domain.model.InventoryItem;
import org.springframework.stereotype.Component;

@Component
public class InventoryMapper {


    public InventoryItem toDomain(InventoryItemEntity entity) {
        if (entity == null) return null;
        InventoryItem item = new InventoryItem();
        item.setId(entity.getId());
        item.setName(entity.getName());
        item.setUnit(entity.getUnit());
        item.setQuantity(entity.getQuantity());
        item.setMinThreshold(entity.getMinThreshold());
        item.setCostPerUnit(entity.getCostPerUnit());
        item.setCategory(entity.getCategory());
        item.setSupplier(entity.getSupplier());
        item.setLastRestockedAt(entity.getLastRestockedAt());
        item.setCreatedAt(entity.getCreatedAt());
        item.setUpdatedAt(entity.getUpdatedAt());
        item.setCreatedBy(entity.getCreatedBy());
        item.setUpdatedBy(entity.getUpdatedBy());
        return item;
    }

    public InventoryItemEntity toEntity(InventoryItem domain) {
        if (domain == null) return null;
        return InventoryItemEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .unit(domain.getUnit())
                .quantity(domain.getQuantity())
                .minThreshold(domain.getMinThreshold())
                .costPerUnit(domain.getCostPerUnit())
                .category(domain.getCategory())
                .supplier(domain.getSupplier())
                .lastRestockedAt(domain.getLastRestockedAt())
                .createdBy(domain.getCreatedBy())
                .updatedBy(domain.getUpdatedBy())
                .build();
    }

}
