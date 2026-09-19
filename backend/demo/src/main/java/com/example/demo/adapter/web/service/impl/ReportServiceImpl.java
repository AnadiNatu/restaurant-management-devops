package com.example.demo.adapter.web.service.impl;

import com.example.demo.adapter.web.dto.ReportDto;
import com.example.demo.adapter.web.service.ReportService;
import com.example.demo.domain.model.enums.OrderStatus;
import com.example.demo.domain.port.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final OrderPort orderPort;
    private final InventoryPort inventoryPort;
    private final CustomerPort customerPort;
    private final MenuItemPort menuItemPort;
    private final UserPort userPort;

    @Override
    @Transactional(readOnly = true)
    public ReportDto.RevenueSummary getWeeklyRevenue() {
        LocalDateTime to = LocalDateTime.now();
        LocalDateTime from = to.minusDays(7);
        return buildRevenueSummary(from, to, "WEEKLY");
    }

    @Override
    @Transactional(readOnly = true)
    public ReportDto.RevenueSummary getMonthlyRevenue() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now.with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0);
        return buildRevenueSummary(from, now, "MONTHLY");
    }

    @Override
    @Transactional(readOnly = true)
    public ReportDto.RevenueSummary getRevenueBetween(LocalDateTime from, LocalDateTime to) {
        return buildRevenueSummary(from, to, "CUSTOM");
    }

    @Override
    @Transactional(readOnly = true)
    public ReportDto.InventoryCostSummary getWeeklyInventoryCost() {
        LocalDateTime to = LocalDateTime.now();
        LocalDateTime from = to.minusDays(7);
        return buildInventorySummary(from, to, "WEEKLY");
    }

    @Override
    @Transactional(readOnly = true)
    public ReportDto.InventoryCostSummary getMonthlyInventoryCost() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now.with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0);
        return buildInventorySummary(from, now, "MONTHLY");
    }

    @Override
    @Transactional(readOnly = true)
    public ReportDto.DashboardSummary getDashboard() {
        LocalDateTime startOfToday = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime now = LocalDateTime.now();

        BigDecimal todayRevenue = orderPort.getTotalRevenueBetween(startOfToday, now);
        long todayOrders = orderPort.findBetween(startOfToday, now).size();
        long pendingOrders = orderPort.findByStatus(OrderStatus.PENDING).size()
                + orderPort.findByStatus(OrderStatus.CONFIRMED).size()
                + orderPort.findByStatus(OrderStatus.PREPARING).size();
        long totalCustomers = customerPort.findAll().size();
        long lowStockItems = inventoryPort.findLowStock().size();
        long activeMenuItems = menuItemPort.findByIsAvailable(true).size();
        long totalEmployees = userPort.findByRole("EMPLOYEE").size();

        ReportDto.DashboardSummary summary = new ReportDto.DashboardSummary();
        summary.setTodayRevenue(todayRevenue != null ? todayRevenue : BigDecimal.ZERO);
        summary.setTodayOrders(todayOrders);
        summary.setPendingOrders(pendingOrders);
        summary.setTotalCustomers(totalCustomers);
        summary.setLowStockItems(lowStockItems);
        summary.setActiveMenuItems(activeMenuItems);
        summary.setTotalEmployees(totalEmployees);
        return summary;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private ReportDto.RevenueSummary buildRevenueSummary(LocalDateTime from, LocalDateTime to, String period) {
        BigDecimal revenue = orderPort.getTotalRevenueBetween(from, to);
        List<?> orders = orderPort.findBetween(from, to);

        ReportDto.RevenueSummary summary = new ReportDto.RevenueSummary();
        summary.setTotalRevenue(revenue != null ? revenue : BigDecimal.ZERO);
        summary.setTotalOrders((long) orders.size());
        summary.setFrom(from);
        summary.setTo(to);
        summary.setPeriod(period);
        return summary;
    }

    private ReportDto.InventoryCostSummary buildInventorySummary(LocalDateTime from, LocalDateTime to, String period) {
        BigDecimal cost = inventoryPort.getTotalCostBetween(from, to);
        int totalItems = inventoryPort.findAll().size();
        int lowStock = inventoryPort.findLowStock().size();

        ReportDto.InventoryCostSummary summary = new ReportDto.InventoryCostSummary();
        summary.setTotalCost(cost != null ? cost : BigDecimal.ZERO);
        summary.setTotalItems(totalItems);
        summary.setLowStockCount(lowStock);
        summary.setFrom(from);
        summary.setTo(to);
        summary.setPeriod(period);
        return summary;
    }
}
