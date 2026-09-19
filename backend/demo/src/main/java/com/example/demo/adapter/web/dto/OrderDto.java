package com.example.demo.adapter.web.dto;

import com.example.demo.domain.model.enums.OrderStatus;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDto {

    @Data
    public static class CreateRequest {
        private Long customerId;
        private String customerName;
        private String customerEmail;
        private String tableNumber;

        @NotEmpty
        private List<OrderItemRequest> items;
        private BigDecimal discountAmount;
        private String notes;
    }

    @Data
    public static class OrderItemRequest {
        @NotNull
        private Long menuItemId;
        @NotNull
        @Min(1)
        private Integer quantity;
    }

    @Data
    public static class UpdateStatusRequest {
        @NotNull
        private OrderStatus status;
        private String notes;
    }

    @Data
    public static class OrderItemResponse {
        private Long id;
        private Long menuItemId;
        private String menuItemName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal taxRate;
        private BigDecimal subtotal;
    }

    @Data
    public static class Response {
        private Long id;
        private Long customerId;
        private String customerName;
        private String customerEmail;
        private String tableNumber;
        private OrderStatus status;
        private List<OrderItemResponse> items;
        private BigDecimal subtotal;
        private BigDecimal taxAmount;
        private BigDecimal discountAmount;
        private BigDecimal totalAmount;
        private String notes;
        private String handledBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    public static class BillResponse {
        private Long orderId;
        private String customerName;
        private String customerEmail;
        private String tableNumber;
        private List<OrderItemResponse> items;
        private BigDecimal subtotal;
        private BigDecimal taxAmount;
        private BigDecimal discountAmount;
        private BigDecimal totalAmount;
        private String handledBy;
        private LocalDateTime billedAt;
        private String invoiceNumber;
    }
}
