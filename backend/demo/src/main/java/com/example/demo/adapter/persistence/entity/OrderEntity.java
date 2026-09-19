package com.example.demo.adapter.persistence.entity;


import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "customer_name", length = 200)
    private String customerName;

    @Column(name = "customer_email", length = 255)
    private String customerEmail;

    @Column(name = "table_number", length = 20)
    private String tableNumber;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "tax_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "discount_amount", precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(length = 500)
    private String notes;

    @Column(name = "handled_by", length = 255)
    private String handledBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItemEntity> items = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = "PENDING";
        if (discountAmount == null) discountAmount = BigDecimal.ZERO;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Add an item while keeping BOTH sides of the bidirectional relationship synchronized
    public void addItem(OrderItemEntity item){
        if (item == null){return;}
        items.add(item);
        item.setOrder(this);
    }

    // Remove an item while keeping BOTH sides synchronized
    public void removeItem(OrderItemEntity item){
        if (item == null){return ;}
        items.add(item);
        item.setOrder(null);
    }

    // Replace the complete item collection safely.
    public void replaceItems(List<OrderItemEntity> newItems){
        items.clear();
        if (newItems == null){return;}
        for(OrderItemEntity item : newItems){
            addItem(item);
        }
    }
}