package com.example.demo.adapter.web.controller;

import com.example.demo.adapter.web.dto.ReportDto;
import com.example.demo.adapter.web.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/dashboard")
    public ResponseEntity<ReportDto.DashboardSummary> getDashboard() {
        return ResponseEntity.ok(reportService.getDashboard());
    }

    @GetMapping("/revenue/weekly")
    public ResponseEntity<ReportDto.RevenueSummary> getWeeklyRevenue() {
        return ResponseEntity.ok(reportService.getWeeklyRevenue());
    }

    @GetMapping("/revenue/monthly")
    public ResponseEntity<ReportDto.RevenueSummary> getMonthlyRevenue() {
        return ResponseEntity.ok(reportService.getMonthlyRevenue());
    }

    @GetMapping("/revenue/between")
    public ResponseEntity<ReportDto.RevenueSummary> getRevenueBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(reportService.getRevenueBetween(from, to));
    }

    @GetMapping("/inventory/weekly")
    public ResponseEntity<ReportDto.InventoryCostSummary> getWeeklyInventoryCost() {
        return ResponseEntity.ok(reportService.getWeeklyInventoryCost());
    }

    @GetMapping("/inventory/monthly")
    public ResponseEntity<ReportDto.InventoryCostSummary> getMonthlyInventoryCost() {
        return ResponseEntity.ok(reportService.getMonthlyInventoryCost());
    }
}
