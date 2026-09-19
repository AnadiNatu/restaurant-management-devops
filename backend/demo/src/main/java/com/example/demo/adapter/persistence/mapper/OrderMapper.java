package com.example.demo.adapter.persistence.mapper;

import com.example.demo.adapter.persistence.entity.OrderEntity;
import com.example.demo.adapter.persistence.entity.OrderItemEntity;
import com.example.demo.domain.model.Order;
import com.example.demo.domain.model.OrderItem;
import com.example.demo.domain.model.enums.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public Order toDomain(OrderEntity entity) {
        if (entity == null) return null;
        Order order = new Order();
        order.setId(entity.getId());
        order.setCustomerId(entity.getCustomerId());
        order.setCustomerName(entity.getCustomerName());
        order.setCustomerEmail(entity.getCustomerEmail());
        order.setTableNumber(entity.getTableNumber());
        order.setStatus(OrderStatus.valueOf(entity.getStatus()));
        order.setSubtotal(entity.getSubtotal());
        order.setTaxAmount(entity.getTaxAmount());
        order.setDiscountAmount(entity.getDiscountAmount());
        order.setTotalAmount(entity.getTotalAmount());
        order.setNotes(entity.getNotes());
        order.setHandledBy(entity.getHandledBy());
        order.setCreatedAt(entity.getCreatedAt());
        order.setUpdatedAt(entity.getUpdatedAt());
        if (entity.getItems() != null) {
            order.setItems(entity.getItems().stream().map(this::itemToDomain).collect(Collectors.toList()));
        }
        return order;
    }

    public OrderItem itemToDomain(OrderItemEntity entity) {
        if (entity == null) return null;
        OrderItem item = new OrderItem();
        item.setId(entity.getId());
        item.setMenuItemId(entity.getMenuItemId());
        item.setMenuItemName(entity.getMenuItemName());
        item.setQuantity(entity.getQuantity());
        item.setUnitPrice(entity.getUnitPrice());
        item.setTaxRate(entity.getTaxRate());
        item.setSubtotal(entity.getSubtotal());
        return item;
    }

    public OrderEntity toEntity(Order domain) {
        if (domain == null) return null;
        OrderEntity entity = new OrderEntity();
        entity.setId(domain.getId());
        entity.setCustomerId(domain.getCustomerId());
        entity.setCustomerName(domain.getCustomerName());
        entity.setCustomerEmail(domain.getCustomerEmail());
        entity.setTableNumber(domain.getTableNumber());
        entity.setStatus(domain.getStatus() != null ? domain.getStatus().name() : "PENDING");
        entity.setSubtotal(domain.getSubtotal());
        entity.setTaxAmount(domain.getTaxAmount());
        entity.setDiscountAmount(domain.getDiscountAmount());
        entity.setTotalAmount(domain.getTotalAmount());
        entity.setNotes(domain.getNotes());
        entity.setHandledBy(domain.getHandledBy());
        if (domain.getItems() != null) {
            for (OrderItem item : domain.getItems()) {
                OrderItemEntity itemEntity = itemToEntity(item, entity);
                if (itemEntity != null) {
                    // VERY IMPORTANT
                    entity.addItem(itemEntity);
                }
            }
//            List<OrderItemEntity> items = domain.getItems().stream()
//                    .map(i -> itemToEntity(i, entity)).collect(Collectors.toList());
//            entity.setItems(items);
        }
        return entity;
    }

    public OrderItemEntity itemToEntity(OrderItem item, OrderEntity parent) {
        if (item == null) return null;
        OrderItemEntity entity = new OrderItemEntity();
        entity.setId(item.getId());
//        entity.setOrder(parent);
        entity.setMenuItemId(item.getMenuItemId());
        entity.setMenuItemName(item.getMenuItemName());
        entity.setQuantity(item.getQuantity());
        entity.setUnitPrice(item.getUnitPrice());
        entity.setTaxRate(item.getTaxRate());
        entity.setSubtotal(item.getSubtotal());
        return entity;
    }
}
