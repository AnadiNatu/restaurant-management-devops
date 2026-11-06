package com.example.demo.adapter.web.service.impl;


import com.example.demo.adapter.web.dto.InventoryDto;
import com.example.demo.adapter.web.service.InventoryService;
import com.example.demo.domain.model.InventoryItem;
import com.example.demo.domain.model.InventoryTransaction;
import com.example.demo.domain.model.enums.InventoryTransactionType;
import com.example.demo.domain.port.InventoryPort;
import com.example.demo.domain.port.InventoryTransactionPort;
import com.example.demo.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Kolkata");
    private static final int COST_SCALE = 2;
    private static final int QUANTITY_SCALE = 3;

    private static final Logger log = LoggerFactory.getLogger(InventoryServiceImpl.class);

    private final InventoryPort inventoryPort;
    private final InventoryTransactionPort transactionPort;

    @Override
    @Transactional
    public InventoryDto.Response create(InventoryDto.CreateRequest request, String createdBy) {
        validatePositiveOrZero(request.getQuantity(), "Initial quantity");
        validatePositive(request.getCostPerUnit(), "Cost per unit");

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

        if (saved.getQuantity().signum() > 0) {
            saveTransaction(
                    saved,
                    InventoryTransactionType.OPENING_BALANCE,
                    saved.getQuantity(),
                    saved.getCostPerUnit(),
                    BigDecimal.ZERO,
                    saved.getQuantity(),
                    "Initial stock balance",
                    createdBy
            );
        }

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
//        if (request.getQuantity() != null) item.setQuantity(request.getQuantity());
        if (request.getMinThreshold() != null) item.setMinThreshold(request.getMinThreshold());
//        if (request.getCostPerUnit() != null) item.setCostPerUnit(request.getCostPerUnit());
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
        validatePositive(request.getQuantity(), "Restock quantity");
        validatePositiveOrZero(request.getUnitCost(), "Purchase unit cost");

        InventoryItem item = inventoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("InventoryItem", "id", id));

        BigDecimal oldQuantity = item.getQuantity();
        BigDecimal oldCost = item.getCostPerUnit();

        BigDecimal addedQuantity = scaleQuantity(request.getQuantity());
        BigDecimal purchaseCost = scaleCost(request.getUnitCost());
        BigDecimal newQuantity = oldQuantity.add(addedQuantity);
        BigDecimal oldStockValue = oldQuantity.multiply(oldCost);
        BigDecimal purchaseValue = addedQuantity.multiply(purchaseCost);
        BigDecimal newAverageCost = oldStockValue.add(purchaseValue).divide(newQuantity, COST_SCALE, RoundingMode.HALF_UP);

        item.setQuantity(newQuantity);
        item.setCostPerUnit(newAverageCost);
        item.setLastRestockedAt(LocalDateTime.now(ZONE));
        item.setUpdatedBy(updatedBy);

        if (request.getSupplier() != null) {
            item.setSupplier(request.getSupplier());
        }

        InventoryItem saved = inventoryPort.save(item);

        saveTransaction(
                saved,
                InventoryTransactionType.RESTOCK,
                addedQuantity,
                purchaseCost,
                oldQuantity,
                newQuantity,
                request.getNotes() != null ? request.getNotes() : "Inventory restocked",
                updatedBy);

        log.info("[INVENTORY] Restocked item={} quantity={} purchaseCost={}", id, addedQuantity, purchaseCost);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public InventoryDto.Response consume(Long id, InventoryDto.ConsumeRequest request, String consumedBy) {
        validatePositive(request.getQuantity(), "Consumption quantity");

        InventoryItem item = inventoryPort.findByIdForUpdate(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("InventoryItem", "id", id));

        BigDecimal consumedQuantity = scaleQuantity(request.getQuantity());
        BigDecimal oldQuantity = item.getQuantity();

        if (oldQuantity.compareTo(consumedQuantity) < 0) {
            throw new IllegalArgumentException(
                    "Insufficient inventory. Available: " + oldQuantity
                            + ", requested: " + consumedQuantity
            );
        }

        BigDecimal averageCost = item.getCostPerUnit();
        BigDecimal consumptionCost = consumedQuantity
                .multiply(averageCost)
                .setScale(COST_SCALE, RoundingMode.HALF_UP);

        BigDecimal newQuantity = oldQuantity.subtract(consumedQuantity);

        item.setQuantity(newQuantity);
        item.setUpdatedBy(consumedBy);

        InventoryItem saved = inventoryPort.save(item);

        saveTransaction(
                saved,
                InventoryTransactionType.CONSUMPTION,
                consumedQuantity,
                averageCost,
                oldQuantity,
                newQuantity,
                request.getNotes(),
                consumedBy
        );

        log.info("[INVENTORY] Consumed item={} quantity={} cost={}",
                id, consumedQuantity, consumptionCost);

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
    public List<InventoryDto.TransactionResponse> getTransactions(Long id) {
        inventoryPort.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("InventoryItem", "id", id));

        return transactionPort.findByInventoryItemId(id)
                .stream()
                .map(this::toTransactionResponse)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional(readOnly = true)
    public InventoryDto.CostSummaryResponse getWeeklyCost() {
        LocalDate today = LocalDate.now(ZONE);
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        return getCostBetween(monday.atStartOfDay(), LocalDateTime.now(ZONE));
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryDto.CostSummaryResponse getMonthlyCost() {
        LocalDate today = LocalDate.now(ZONE);

        LocalDateTime from = today
                .with(TemporalAdjusters.firstDayOfMonth())
                .atStartOfDay();

        return getCostBetween(from, LocalDateTime.now(ZONE));
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryDto.CostSummaryResponse getCostBetween(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null || !from.isBefore(to)) {
            throw new IllegalArgumentException(
                    "Valid from and to dates are required; from must be before to"
            );
        }

        BigDecimal cost = inventoryPort.getTotalCostBetween(from, to);

        InventoryDto.CostSummaryResponse response =
                new InventoryDto.CostSummaryResponse();

        response.setTotalCost(
                cost != null ? cost : BigDecimal.ZERO.setScale(COST_SCALE)
        );
        response.setFrom(from);
        response.setTo(to);
        response.setPeriod("CUSTOM");

        return response;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        inventoryPort.deleteById(id);
        log.info("[INVENTORY] Item deleted | id={}", id);
    }

    private void saveTransaction(InventoryItem item, InventoryTransactionType type, BigDecimal quantity, BigDecimal unitCost,
            BigDecimal before, BigDecimal after, String notes, String actor) {
        InventoryTransaction transaction = new InventoryTransaction();

        transaction.setInventoryItemId(item.getId());
        transaction.setTransactionType(type);
        transaction.setQuantity(scaleQuantity(quantity));
        transaction.setUnitCost(scaleCost(unitCost));
        transaction.setTotalCost(quantity.multiply(unitCost)
                        .setScale(COST_SCALE, RoundingMode.HALF_UP));
        transaction.setQuantityBefore(before);
        transaction.setQuantityAfter(after);
        transaction.setNotes(notes);
        transaction.setCreatedBy(actor);
        transaction.setCreatedAt(LocalDateTime.now(ZONE));

        transactionPort.save(transaction);
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

    private InventoryDto.TransactionResponse toTransactionResponse(
            InventoryTransaction transaction
    ) {
        InventoryDto.TransactionResponse response =
                new InventoryDto.TransactionResponse();

        response.setId(transaction.getId());
        response.setInventoryItemId(transaction.getInventoryItemId());
        response.setTransactionType(
                transaction.getTransactionType().name()
        );
        response.setQuantity(transaction.getQuantity());
        response.setUnitCost(transaction.getUnitCost());
        response.setTotalCost(transaction.getTotalCost());
        response.setQuantityBefore(transaction.getQuantityBefore());
        response.setQuantityAfter(transaction.getQuantityAfter());
        response.setReferenceOrderId(transaction.getReferenceOrderId());
        response.setNotes(transaction.getNotes());
        response.setCreatedBy(transaction.getCreatedBy());
        response.setCreatedAt(transaction.getCreatedAt());

        return response;
    }

    private void validatePositive(BigDecimal value, String field) {
        if (value == null || value.signum() <= 0) {
            throw new IllegalArgumentException(field + " must be greater than zero");
        }
    }

    private void validatePositiveOrZero(BigDecimal value, String field) {
        if (value == null || value.signum() < 0) {
            throw new IllegalArgumentException(field + " cannot be negative");
        }
    }

    private BigDecimal scaleCost(BigDecimal value) {
        return value.setScale(COST_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal scaleQuantity(BigDecimal value) {
        return value.setScale(QUANTITY_SCALE, RoundingMode.HALF_UP);
    }
}