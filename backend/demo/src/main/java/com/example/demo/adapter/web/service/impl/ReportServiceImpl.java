package com.example.demo.adapter.web.service.impl;

import com.example.demo.adapter.web.dto.ReportDto;
import com.example.demo.adapter.web.service.ReportService;
import com.example.demo.config.RevenueReportPdfGenerator;
import com.example.demo.domain.model.Order;
import com.example.demo.domain.model.enums.OrderStatus;
import com.example.demo.domain.port.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final OrderPort orderPort;
    private final InventoryPort inventoryPort;
    private final CustomerPort customerPort;
    private final MenuItemPort menuItemPort;
    private final UserPort userPort;
    private final RevenueReportPdfGenerator pdfGenerator;

    @Override
    @Transactional(readOnly = true)
    public ReportDto.RevenueSummary getWeeklyRevenue() {
        ZoneId zone = ZoneId.of("Asia/Kolkata");
        LocalDate today = LocalDate.now(zone);
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDateTime to = LocalDateTime.now(zone);
        LocalDateTime from = monday.atStartOfDay();
        return buildRevenueSummary(from, to, "WEEKLY");
    }

    @Override
    @Transactional(readOnly = true)
    public ReportDto.RevenueSummary getMonthlyRevenue() {
        ZoneId zone = ZoneId.of("Asia/Kolkata");
        LocalDate today = LocalDate.now(zone);
        LocalDateTime now = LocalDateTime.now(zone);
        LocalDateTime from = today.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
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

//        BigDecimal todayRevenue = orderPort.getTotalRevenueBetween(startOfToday, now);
//        long todayOrders = orderPort.findBetween(startOfToday, now).size();

        ReportDto.RevenueSummary todaySummary = buildRevenueSummary(startOfToday,now, "TODAY");
        BigDecimal todayRevenue = todaySummary.getTotalInvoiceValue();
        long todayOrders = todaySummary.getTotalOrders();

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

    @Override
    @Transactional(readOnly = true)
    public byte[] generateWeeklyRevenuePdf() {

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDateTime from = monday.atStartOfDay();
        LocalDateTime to = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));

        ReportDto.RevenueSummary report = buildRevenueSummary(from, to, "WEEKLY");
        return pdfGenerator.generate(report);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateMonthlyRevenuePdf() {

        ZoneId zone = ZoneId.of("Asia/Kolkata");

        LocalDate today = LocalDate.now(zone);
        LocalDateTime from = today.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
        LocalDateTime to = LocalDateTime.now(zone);

        ReportDto.RevenueSummary report = buildRevenueSummary(from, to, "MONTHLY");

        return pdfGenerator.generate(report);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateRevenuePdf(LocalDateTime from, LocalDateTime to) {

        if (from == null || to == null) {
            throw new IllegalArgumentException("Both from and to dates are required");
        }

        if (!from.isBefore(to)) {
            throw new IllegalArgumentException("'from' must be before 'to'");
        }

        ReportDto.RevenueSummary report = buildRevenueSummary(from, to, "CUSTOM");
        return pdfGenerator.generate(report);
    }


    // ── Helpers ──────────────────────────────────────────────────────────────

//    private ReportDto.RevenueSummary buildRevenueSummary(LocalDateTime from, LocalDateTime to, String period) {
//        BigDecimal revenue = orderPort.getTotalRevenueBetween(from, to);
//        List<?> orders = orderPort.findBetween(from, to);
//
//        ReportDto.RevenueSummary summary = new ReportDto.RevenueSummary();
//        summary.setTotalRevenue(revenue != null ? revenue : BigDecimal.ZERO);
//        summary.setTotalOrders((long) orders.size());
//        summary.setFrom(from);
//        summary.setTo(to);
//        summary.setPeriod(period);
//        return summary;
//    }


    private ReportDto.RevenueSummary buildRevenueSummary(
            LocalDateTime from,
            LocalDateTime to,
            String period
    ) {
        if (from == null || to == null || !from.isBefore(to)) {
            throw new IllegalArgumentException(
                    "A valid reporting period is required"
            );
        }

        List<Order> orders = orderPort.findBetween(from, to);

        BigDecimal grossSales = BigDecimal.ZERO;
        BigDecimal discounts = BigDecimal.ZERO;
        BigDecimal taxableSales = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;
        BigDecimal totalInvoiceValue = BigDecimal.ZERO;

        BigDecimal totalCgst = BigDecimal.ZERO;
        BigDecimal totalSgst = BigDecimal.ZERO;
        BigDecimal totalIgst = BigDecimal.ZERO;

        long validOrders = 0;
        long missingGstBreakdownOrders = 0;

        for (Order order : orders) {
            // Only billed orders are counted as sales.
            if (order.getStatus() != OrderStatus.BILLED) {
                continue;
            }

            BigDecimal subtotal = money(order.getSubtotal());
            BigDecimal discount = money(order.getDiscountAmount());
            BigDecimal tax = money(order.getTaxAmount());
            BigDecimal total = money(order.getTotalAmount());

            BigDecimal cgst = money(order.getCgstAmount());
            BigDecimal sgst = money(order.getSgstAmount());
            BigDecimal igst = money(order.getIgstAmount());

            if (subtotal.signum() < 0
                    || discount.signum() < 0
                    || tax.signum() < 0
                    || total.signum() < 0
                    || cgst.signum() < 0
                    || sgst.signum() < 0
                    || igst.signum() < 0) {
                throw new IllegalStateException(
                        "Negative financial amount for order " + order.getId()
                );
            }

            if (discount.compareTo(subtotal) > 0) {
                throw new IllegalStateException(
                        "Discount exceeds subtotal for order " + order.getId()
                );
            }

            BigDecimal taxable = money(subtotal.subtract(discount));
            BigDecimal expectedTotal = money(taxable.add(tax));

            if (expectedTotal.compareTo(total) != 0) {
                throw new IllegalStateException(
                        "Financial totals do not reconcile for order "
                                + order.getId()
                                + ". Expected invoice total: "
                                + expectedTotal
                                + ", stored total: "
                                + total
                );
            }

            BigDecimal taxComponents = money(cgst.add(sgst).add(igst));

            if (taxComponents.compareTo(tax) != 0) {
                missingGstBreakdownOrders++;

                log.warn(
                        "GST breakdown does not reconcile for order {}. "
                                + "Stored tax={}, CGST={}, SGST={}, IGST={}. "
                                + "Check or backfill this historical record.",
                        order.getId(),
                        tax,
                        cgst,
                        sgst,
                        igst
                );
            }

            grossSales = grossSales.add(subtotal);
            discounts = discounts.add(discount);
            taxableSales = taxableSales.add(taxable);
            totalTax = totalTax.add(tax);

            totalCgst = totalCgst.add(cgst);
            totalSgst = totalSgst.add(sgst);
            totalIgst = totalIgst.add(igst);

            totalInvoiceValue = totalInvoiceValue.add(total);

            validOrders++;
        }

        ReportDto.RevenueSummary summary =
                new ReportDto.RevenueSummary();

        summary.setGrossSales(money(grossSales));
        summary.setTotalDiscount(money(discounts));
        summary.setTaxableSales(money(taxableSales));

        // Revenue excludes GST.
        summary.setTotalRevenue(money(taxableSales));

        summary.setTotalTax(money(totalTax));
        summary.setTotalCgst(money(totalCgst));
        summary.setTotalSgst(money(totalSgst));
        summary.setTotalIgst(money(totalIgst));

        // Invoice value includes GST.
        summary.setTotalInvoiceValue(money(totalInvoiceValue));

        summary.setTotalOrders(validOrders);
        summary.setFrom(from);
        summary.setTo(to);
        summary.setPeriod(period);

        log.info(
                "Revenue report built: period={}, from={}, to={}, "
                        + "orders={}, taxableSales={}, tax={}, invoiceValue={}, "
                        + "ordersWithGstBreakdownMismatch={}",
                period,
                from,
                to,
                validOrders,
                money(taxableSales),
                money(totalTax),
                money(totalInvoiceValue),
                missingGstBreakdownOrders
        );

        return summary;
    }

    private BigDecimal money(BigDecimal value) {
        return value == null
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : value.setScale(2, RoundingMode.HALF_UP);
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
