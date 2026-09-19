package com.example.demo.adapter.web.dto;


import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ReportDto {

    @Data
    public static class RevenueSummary {
        private BigDecimal totalRevenue;
        private BigDecimal totalTax;
        private Long totalOrders;
        private LocalDateTime from;
        private LocalDateTime to;
        private String period;
    }

    @Data
    public static class InventoryCostSummary {
        private BigDecimal totalCost;
        private Integer totalItems;
        private Integer lowStockCount;
        private LocalDateTime from;
        private LocalDateTime to;
        private String period;
    }

    @Data
    public static class DashboardSummary {
        private BigDecimal todayRevenue;
        private Long todayOrders;
        private Long pendingOrders;
        private Long totalCustomers;
        private Long lowStockItems;
        private Long activeMenuItems;
        private Long totalEmployees;
    }
}
