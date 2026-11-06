package com.example.demo.domain.model;

import com.example.demo.domain.model.enums.InventoryTransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InventoryTransaction {
    private Long id;
    private Long inventoryItemId;

    private InventoryTransactionType transactionType;

    private BigDecimal quantity;
    private BigDecimal unitCost;
    private BigDecimal totalCost;

    private BigDecimal quantityBefore;
    private BigDecimal quantityAfter;

    private Long referenceOrderId;
    private String notes;
    private String createdBy;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getInventoryItemId() { return inventoryItemId; }
    public void setInventoryItemId(Long inventoryItemId) {
        this.inventoryItemId = inventoryItemId;
    }

    public InventoryTransactionType getTransactionType() {
        return transactionType;
    }
    public void setTransactionType(InventoryTransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }

    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }

    public BigDecimal getQuantityBefore() { return quantityBefore; }
    public void setQuantityBefore(BigDecimal quantityBefore) {
        this.quantityBefore = quantityBefore;
    }

    public BigDecimal getQuantityAfter() { return quantityAfter; }
    public void setQuantityAfter(BigDecimal quantityAfter) {
        this.quantityAfter = quantityAfter;
    }

    public Long getReferenceOrderId() { return referenceOrderId; }
    public void setReferenceOrderId(Long referenceOrderId) {
        this.referenceOrderId = referenceOrderId;
    }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
