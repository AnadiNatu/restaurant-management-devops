package com.example.demo.adapter.web.service.impl;


import com.example.demo.adapter.web.dto.InventoryDto;
import com.example.demo.adapter.web.service.InventoryService;
import com.example.demo.domain.model.InventoryItem;
import com.example.demo.domain.port.InventoryPort;
import com.example.demo.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryServiceImpl.class);

    private final InventoryPort inventoryPort;

    @Override
    @Transactional
    public InventoryDto.Response create(InventoryDto.CreateRequest request, String createdBy) {
        InventoryItem item = new InventoryItem();
        item.setName(request.getName());
        item.setUnit(request.getUnit());
        item.setQuantity(request.getQuantity());
        item.setMinThreshold(request.getMinThreshold());
        item.setCostPerUnit(request.getCostPerUnit());
        item.setCategory(request.getCategory());
        item.setSupplier(request.getSupplier());
        item.setLastRestockedAt(LocalDateTime.now());
        item.setCreatedBy(createdBy);
        item.setUpdatedBy(createdBy);

        InventoryItem saved = inventoryPort.save(item);
        log.info("[INVENTORY] Item created | name={} | by={}", saved.getName(), createdBy);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public InventoryDto.Response update(Long id, InventoryDto.UpdateRequest request, String updatedBy) {
        InventoryItem item = inventoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("InventoryItem", "id", id));

        if (request.getName() != null) item.setName(request.getName());
        if (request.getUnit() != null) item.setUnit(request.getUnit());
        if (request.getQuantity() != null) item.setQuantity(request.getQuantity());
        if (request.getMinThreshold() != null) item.setMinThreshold(request.getMinThreshold());
        if (request.getCostPerUnit() != null) item.setCostPerUnit(request.getCostPerUnit());
        if (request.getCategory() != null) item.setCategory(request.getCategory());
        if (request.getSupplier() != null) item.setSupplier(request.getSupplier());
        item.setUpdatedBy(updatedBy);

        InventoryItem saved = inventoryPort.save(item);
        log.info("[INVENTORY] Item updated | id={} | by={}", id, updatedBy);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public InventoryDto.Response restock(Long id, InventoryDto.RestockRequest request, String updatedBy) {
        InventoryItem item = inventoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("InventoryItem", "id", id));

        item.setQuantity(item.getQuantity().add(request.getQuantity()));
        item.setLastRestockedAt(LocalDateTime.now());
        if (request.getSupplier() != null) item.setSupplier(request.getSupplier());
        item.setUpdatedBy(updatedBy);

        InventoryItem saved = inventoryPort.save(item);
        log.info("[INVENTORY] Restocked | id={} | added={} | by={}", id, request.getQuantity(), updatedBy);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryDto.Response getById(Long id) {
        return inventoryPort.findById(id).map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("InventoryItem", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryDto.Response> getAll() {
        return inventoryPort.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryDto.Response> getLowStock() {
        return inventoryPort.findLowStock().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryDto.CostSummaryResponse getWeeklyCost() {
        LocalDateTime to = LocalDateTime.now();
        LocalDateTime from = to.minusDays(7);
        BigDecimal cost = inventoryPort.getTotalCostBetween(from, to);
        return buildCostSummary(cost, from, to, "WEEKLY");
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryDto.CostSummaryResponse getMonthlyCost() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now.with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime to = now;
        BigDecimal cost = inventoryPort.getTotalCostBetween(from, to);
        return buildCostSummary(cost, from, to, "MONTHLY");
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryDto.CostSummaryResponse getCostBetween(LocalDateTime from, LocalDateTime to) {
        BigDecimal cost = inventoryPort.getTotalCostBetween(from, to);
        return buildCostSummary(cost, from, to, "CUSTOM");
    }

    @Override
    @Transactional
    public void delete(Long id) {
        inventoryPort.deleteById(id);
        log.info("[INVENTORY] Item deleted | id={}", id);
    }

    private InventoryDto.CostSummaryResponse buildCostSummary(BigDecimal cost,
                                                              LocalDateTime from, LocalDateTime to, String period) {
        InventoryDto.CostSummaryResponse r = new InventoryDto.CostSummaryResponse();
        r.setTotalCost(cost != null ? cost : BigDecimal.ZERO);
        r.setFrom(from);
        r.setTo(to);
        r.setPeriod(period);
        return r;
    }

    private InventoryDto.Response toResponse(InventoryItem item) {
        InventoryDto.Response r = new InventoryDto.Response();
        r.setId(item.getId());
        r.setName(item.getName());
        r.setUnit(item.getUnit());
        r.setQuantity(item.getQuantity());
        r.setMinThreshold(item.getMinThreshold());
        r.setCostPerUnit(item.getCostPerUnit());
        r.setTotalValue(item.getQuantity() != null && item.getCostPerUnit() != null
                ? item.getQuantity().multiply(item.getCostPerUnit()) : BigDecimal.ZERO);
        r.setCategory(item.getCategory());
        r.setSupplier(item.getSupplier());
        r.setIsLowStock(item.getQuantity() != null && item.getMinThreshold() != null
                && item.getQuantity().compareTo(item.getMinThreshold()) <= 0);
        r.setLastRestockedAt(item.getLastRestockedAt());
        r.setCreatedAt(item.getCreatedAt());
        r.setUpdatedAt(item.getUpdatedAt());
        r.setCreatedBy(item.getCreatedBy());
        r.setUpdatedBy(item.getUpdatedBy());
        return r;
    }
}