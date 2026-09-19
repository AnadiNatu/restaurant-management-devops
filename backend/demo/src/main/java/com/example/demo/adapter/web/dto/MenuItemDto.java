package com.example.demo.adapter.web.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MenuItemDto {

    @Data
    public static class CreateRequest {
        @NotBlank
        private String name;
        private String description;
        @NotNull
        @DecimalMin("0.01")
        private BigDecimal price;
        @NotBlank
        private String category;
        private Boolean isAvailable;
        private String imageUrl;
        private String taxCategory;
        @DecimalMin("0.0")
        @DecimalMax("100.0")
        private BigDecimal taxRate;
    }

    @Data
    public static class UpdateRequest {
        private String name;
        private String description;
        @DecimalMin("0.01")
        private BigDecimal price;
        private String category;
        private Boolean isAvailable;
        private String imageUrl;
        private String taxCategory;
        @DecimalMin("0.0")
        @DecimalMax("100.0")
        private BigDecimal taxRate;
    }

    @Data
    public static class Response {
        private Long id;
        private String name;
        private String description;
        private BigDecimal price;
        private String category;
        private Boolean isAvailable;
        private String imageUrl;
        private String taxCategory;
        private BigDecimal taxRate;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String createdBy;
        private String updatedBy;
    }
}
