package com.example.demo.adapter.persistence.mapper;

import com.example.demo.adapter.persistence.entity.MenuItemEntity;
import com.example.demo.domain.model.MenuItem;
import org.springframework.stereotype.Component;

@Component
public class MenuItemMapper {

    public MenuItem toDomain(MenuItemEntity entity) {
        if (entity == null) return null;
        MenuItem item = new MenuItem();
        item.setId(entity.getId());
        item.setName(entity.getName());
        item.setDescription(entity.getDescription());
        item.setPrice(entity.getPrice());
        item.setCategory(entity.getCategory());
        item.setIsAvailable(entity.getIsAvailable());
        item.setImageUrl(entity.getImageUrl());
        item.setTaxCategory(entity.getTaxCategory());
        item.setTaxRate(entity.getTaxRate());
        item.setCreatedAt(entity.getCreatedAt());
        item.setUpdatedAt(entity.getUpdatedAt());
        item.setCreatedBy(entity.getCreatedBy());
        item.setUpdatedBy(entity.getUpdatedBy());
        return item;
    }

    public MenuItemEntity toEntity(MenuItem domain) {
        if (domain == null) return null;
        return MenuItemEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .description(domain.getDescription())
                .price(domain.getPrice())
                .category(domain.getCategory())
                .isAvailable(domain.getIsAvailable() != null ? domain.getIsAvailable() : true)
                .imageUrl(domain.getImageUrl())
                .taxCategory(domain.getTaxCategory())
                .taxRate(domain.getTaxRate())
                .createdBy(domain.getCreatedBy())
                .updatedBy(domain.getUpdatedBy())
                .build();
    }

}
