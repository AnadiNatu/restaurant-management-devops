package com.example.demo.adapter.web.controller;

import com.example.demo.adapter.security.RestaurantUserDetails;
import com.example.demo.adapter.web.dto.MenuItemDto;
import com.example.demo.adapter.web.service.ActivityLogService;
import com.example.demo.adapter.web.service.MenuService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;
    private final ActivityLogService activityLogService;

    // ── Read (all roles) ─────────────────────────────────────────────────────

    @GetMapping("/getAll")
    public ResponseEntity<List<MenuItemDto.Response>> getAll() {
        return ResponseEntity.ok(menuService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuItemDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(menuService.getById(id));
    }

    @GetMapping("/available")
    public ResponseEntity<List<MenuItemDto.Response>> getAvailable() {
        return ResponseEntity.ok(menuService.getAvailable());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<MenuItemDto.Response>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(menuService.getByCategory(category));
    }

    // ── Write (ADMIN + SUPER_ADMIN) ──────────────────────────────────────────

//    Caused by: org.hibernate.exception.ConstraintViolationException: could not execute statement [ERROR: duplicate key value violates unique constraint "menu_items_pkey"
//  Detail: Key (id)=(1) already exists.] [insert into menu_items (category,created_at,created_by,description,image_url,is_available,name,price,tax_category,tax_rate,updated_at,updated_by) values (?,?,?,?,?,?,?,?,?,?,?,?)]
//	at org.hibernate.exception.internal.SQLStateConversionDelegate.convert(SQLStateConversionDelegate.java:97)
//	at org.hibernate.exception.internal.StandardSQLExceptionConverter.convert(StandardSQLExceptionConverter.java:58)
//	at org.hibernate.engine.jdbc.spi.SqlExceptionHelper.convert(SqlExceptionHelper.java:108)
//	at org.hibernate.engine.jdbc.internal.ResultSetReturnImpl.executeUpdate(ResultSetReturnImpl.java:197)
//	at org.hibernate.id.insert.GetGeneratedKeysDelegate.performInsert(GetGeneratedKeysDelegate.java:107)
//	at org.hibernate.engine.jdbc.mutation.internal.MutationExecutorPostInsertSingleTable.execute(MutationExecutorPostInsertSingleTable.java:100)
//	at org.hibernate.persister.entity.mutation.InsertCoordinator.doStaticInserts(InsertCoordinator.java:175)
//	at org.hibernate.persister.entity.mutation.InsertCoordinator.coordinateInsert(InsertCoordinator.java:113)
    @PostMapping("/create")
    public ResponseEntity<MenuItemDto.Response> create(
            @Valid @RequestBody MenuItemDto.CreateRequest request,
            @AuthenticationPrincipal RestaurantUserDetails principal,
            HttpServletRequest httpRequest) {

        String actor = principal.getUsername();
        MenuItemDto.Response response = menuService.create(request, actor);
        activityLogService.log(actor, principal.getUser().getRole(),
                "CREATE_MENU_ITEM", "MENU_ITEM", String.valueOf(response.getId()),
                "Created menu item: " + response.getName(), httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MenuItemDto.Response> update(
            @PathVariable Long id,
            @Valid @RequestBody MenuItemDto.UpdateRequest request,
            @AuthenticationPrincipal RestaurantUserDetails principal,
            HttpServletRequest httpRequest) {

        String actor = principal.getUsername();
        MenuItemDto.Response response = menuService.update(id, request, actor);
        activityLogService.log(actor, principal.getUser().getRole(),
                "UPDATE_MENU_ITEM", "MENU_ITEM", String.valueOf(id),
                "Updated menu item: " + response.getName(), httpRequest);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/toggle-availability")
    public ResponseEntity<MenuItemDto.Response> toggleAvailability(
            @PathVariable Long id,
            @AuthenticationPrincipal RestaurantUserDetails principal,
            HttpServletRequest httpRequest) {

        String actor = principal.getUsername();
        MenuItemDto.Response response = menuService.toggleAvailability(id, actor);
        activityLogService.log(actor, principal.getUser().getRole(),
                "TOGGLE_MENU_AVAILABILITY", "MENU_ITEM", String.valueOf(id),
                "Toggled availability for: " + response.getName() + " -> " + response.getIsAvailable(), httpRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal RestaurantUserDetails principal,
            HttpServletRequest httpRequest) {

        String actor = principal.getUsername();
        menuService.delete(id);
        activityLogService.log(actor, principal.getUser().getRole(),
                "DELETE_MENU_ITEM", "MENU_ITEM", String.valueOf(id),
                "Deleted menu item id: " + id, httpRequest);
        return ResponseEntity.noContent().build();
    }

}
