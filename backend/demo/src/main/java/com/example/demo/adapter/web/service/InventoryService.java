package com.example.demo.adapter.web.service;

import com.example.demo.adapter.web.dto.InventoryDto;

import java.time.LocalDateTime;
import java.util.List;

public interface InventoryService {
    InventoryDto.Response create(InventoryDto.CreateRequest request, String createdBy);
    InventoryDto.Response update(Long id, InventoryDto.UpdateRequest request, String updatedBy);
    InventoryDto.Response restock(Long id, InventoryDto.RestockRequest request, String updatedBy);
    InventoryDto.Response getById(Long id);
    List<InventoryDto.Response> getAll();
    List<InventoryDto.Response> getLowStock();
    InventoryDto.CostSummaryResponse getWeeklyCost();
    InventoryDto.CostSummaryResponse getMonthlyCost();
    InventoryDto.CostSummaryResponse getCostBetween(LocalDateTime from, LocalDateTime to);
    void delete(Long id);
}
