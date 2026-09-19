package com.example.demo.adapter.web.dto;


import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InventoryDto {

    @Data
    public static class CreateRequest {
        @NotBlank
        private String name;
        @NotBlank
        private String unit;
        @NotNull
        @DecimalMin("0.0")
        private BigDecimal quantity;
        @NotNull
        @DecimalMin("0.0")
        private BigDecimal minThreshold;
        @NotNull
        @DecimalMin("0.01")
        private BigDecimal costPerUnit;
        private String category;
        private String supplier;
    }

    @Data
    public static class UpdateRequest {
        private String name;
        private String unit;
        @DecimalMin("0.0")
        private BigDecimal quantity;
        @DecimalMin("0.0")
        private BigDecimal minThreshold;
        @DecimalMin("0.01")
        private BigDecimal costPerUnit;
        private String category;
        private String supplier;
    }

    @Data
    public static class RestockRequest {
        @NotNull
        @DecimalMin("0.01")
        private BigDecimal quantity;

        private String supplier;
    }

    @Data
    public static class Response {
        private Long id;
        private String name;
        private String unit;
        private BigDecimal quantity;
        private BigDecimal minThreshold;
        private BigDecimal costPerUnit;
        private BigDecimal totalValue;
        private String category;
        private String supplier;
        private Boolean isLowStock;
        private LocalDateTime lastRestockedAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String createdBy;
        private String updatedBy;
    }

    @Data
    public static class CostSummaryResponse {
        private BigDecimal totalCost;
        private LocalDateTime from;
        private LocalDateTime to;
        private String period;
    }
}