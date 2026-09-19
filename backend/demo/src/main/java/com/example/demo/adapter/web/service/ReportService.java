package com.example.demo.adapter.web.service;

import com.example.demo.adapter.web.dto.ReportDto;

import java.time.LocalDateTime;

public interface ReportService {
    ReportDto.RevenueSummary getWeeklyRevenue();
    ReportDto.RevenueSummary getMonthlyRevenue();
    ReportDto.RevenueSummary getRevenueBetween(LocalDateTime from, LocalDateTime to);
    ReportDto.InventoryCostSummary getWeeklyInventoryCost();
    ReportDto.InventoryCostSummary getMonthlyInventoryCost();
    ReportDto.DashboardSummary getDashboard();
}
