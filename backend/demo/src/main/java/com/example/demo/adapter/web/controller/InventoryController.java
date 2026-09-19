package com.example.demo.adapter.web.controller;

import com.example.demo.adapter.security.RestaurantUserDetails;
import com.example.demo.adapter.web.dto.InventoryDto;
import com.example.demo.adapter.web.service.ActivityLogService;
import com.example.demo.adapter.web.service.InventoryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final ActivityLogService activityLogService;

    // ── Read (all roles) ─────────────────────────────────────────────────────

    @GetMapping("/getAll")
    public ResponseEntity<List<InventoryDto.Response>> getAll() {
        return ResponseEntity.ok(inventoryService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getById(id));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryDto.Response>> getLowStock() {
        return ResponseEntity.ok(inventoryService.getLowStock());
    }

    @GetMapping("/cost/weekly")
    public ResponseEntity<InventoryDto.CostSummaryResponse> getWeeklyCost() {
        return ResponseEntity.ok(inventoryService.getWeeklyCost());
    }

    @GetMapping("/cost/monthly")
    public ResponseEntity<InventoryDto.CostSummaryResponse> getMonthlyCost() {
        return ResponseEntity.ok(inventoryService.getMonthlyCost());
    }

    @GetMapping("/cost/between")
    public ResponseEntity<InventoryDto.CostSummaryResponse> getCostBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(inventoryService.getCostBetween(from, to));
    }

    // ── Write (ADMIN + SUPER_ADMIN) ──────────────────────────────────────────

    @PostMapping("/create")
    public ResponseEntity<InventoryDto.Response> create(
            @Valid @RequestBody InventoryDto.CreateRequest request,
            @AuthenticationPrincipal RestaurantUserDetails principal,
            HttpServletRequest httpRequest) {

        String actor = principal.getUsername();
        InventoryDto.Response response = inventoryService.create(request, actor);
        activityLogService.log(actor, principal.getUser().getRole(),
                "CREATE_INVENTORY_ITEM", "INVENTORY", String.valueOf(response.getId()),
                "Created inventory item: " + response.getName(), httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventoryDto.Response> update(
            @PathVariable Long id,
            @Valid @RequestBody InventoryDto.UpdateRequest request,
            @AuthenticationPrincipal RestaurantUserDetails principal,
            HttpServletRequest httpRequest) {

        String actor = principal.getUsername();
        InventoryDto.Response response = inventoryService.update(id, request, actor);
        activityLogService.log(actor, principal.getUser().getRole(),
                "UPDATE_INVENTORY_ITEM", "INVENTORY", String.valueOf(id),
                "Updated inventory item: " + response.getName(), httpRequest);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/restock")
    public ResponseEntity<InventoryDto.Response> restock(
            @PathVariable Long id,
            @Valid @RequestBody InventoryDto.RestockRequest request,
            @AuthenticationPrincipal RestaurantUserDetails principal,
            HttpServletRequest httpRequest) {

        String actor = principal.getUsername();
        InventoryDto.Response response = inventoryService.restock(id, request, actor);
        activityLogService.log(actor, principal.getUser().getRole(),
                "RESTOCK_INVENTORY", "INVENTORY", String.valueOf(id),
                "Restocked item: " + response.getName() + " | qty added: " + request.getQuantity(), httpRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal RestaurantUserDetails principal,
            HttpServletRequest httpRequest) {

        String actor = principal.getUsername();
        inventoryService.delete(id);
        activityLogService.log(actor, principal.getUser().getRole(),
                "DELETE_INVENTORY_ITEM", "INVENTORY", String.valueOf(id),
                "Deleted inventory item id: " + id, httpRequest);
        return ResponseEntity.noContent().build();
    }

}