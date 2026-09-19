package com.example.demo.adapter.web.controller;

import com.example.demo.adapter.security.RestaurantUserDetails;
import com.example.demo.adapter.web.dto.UserManagementDto;
import com.example.demo.adapter.web.service.ActivityLogService;
import com.example.demo.adapter.web.service.UserManagementService;
import com.example.demo.domain.model.enums.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/super-admin")
@RequiredArgsConstructor
public class SuperAdminController {

    private final UserManagementService userManagementService;
    private final ActivityLogService activityLogService;

    // ── User CRUD ─────────────────────────────────────────────────────────────

    @PostMapping("/create/users")
    public ResponseEntity<UserManagementDto.Response> createUser(
            @Valid @RequestBody UserManagementDto.CreateUserRequest request,
            @AuthenticationPrincipal RestaurantUserDetails principal,
            HttpServletRequest httpRequest) {

        String actor = principal.getUsername();
        UserManagementDto.Response response = userManagementService.createUser(request, actor);
        activityLogService.log(actor, UserRole.SUPER_ADMIN,
                "CREATE_USER", "USER", String.valueOf(response.getId()),
                "Created user: " + response.getEmail() + " | role: " + response.getRole(), httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserManagementDto.Response>> getAllUsers() {
        return ResponseEntity.ok(userManagementService.getAll());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserManagementDto.Response> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userManagementService.getById(id));
    }

    @GetMapping("/users/role/{role}")
    public ResponseEntity<List<UserManagementDto.Response>> getByRole(@PathVariable UserRole role) {
        return ResponseEntity.ok(userManagementService.getByRole(role));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserManagementDto.Response> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserManagementDto.UpdateUserRequest request) {
        return ResponseEntity.ok(userManagementService.updateUser(id, request));
    }

    // ── Role management ───────────────────────────────────────────────────────

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<UserManagementDto.Response> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody UserManagementDto.UpdateRoleRequest request,
            @AuthenticationPrincipal RestaurantUserDetails principal,
            HttpServletRequest httpRequest) {

        String actor = principal.getUsername();
        UserManagementDto.Response response = userManagementService.updateRole(id, request, actor);
        activityLogService.log(actor, UserRole.SUPER_ADMIN,
                "UPDATE_USER_ROLE", "USER", String.valueOf(id),
                "Role updated to: " + request.getRole(), httpRequest);
        return ResponseEntity.ok(response);
    }

    // ── Activation / Deactivation ─────────────────────────────────────────────

    @PatchMapping("/users/{id}/activate")
    public ResponseEntity<String> activate(
            @PathVariable Long id,
            @AuthenticationPrincipal RestaurantUserDetails principal,
            HttpServletRequest httpRequest) {

        String actor = principal.getUsername();
        userManagementService.activate(id, actor);
        activityLogService.log(actor, UserRole.SUPER_ADMIN,
                "ACTIVATE_USER", "USER", String.valueOf(id), "User activated", httpRequest);
        return ResponseEntity.ok("User activated.");
    }

    @PatchMapping("/users/{id}/deactivate")
    public ResponseEntity<String> deactivate(
            @PathVariable Long id,
            @AuthenticationPrincipal RestaurantUserDetails principal,
            HttpServletRequest httpRequest) {

        String actor = principal.getUsername();
        userManagementService.deactivate(id, actor);
        activityLogService.log(actor, UserRole.SUPER_ADMIN,
                "DEACTIVATE_USER", "USER", String.valueOf(id), "User deactivated", httpRequest);
        return ResponseEntity.ok("User deactivated.");
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal RestaurantUserDetails principal,
            HttpServletRequest httpRequest) {

        String actor = principal.getUsername();
        userManagementService.delete(id, actor);
        activityLogService.log(actor, UserRole.SUPER_ADMIN,
                "DELETE_USER", "USER", String.valueOf(id), "User permanently deleted", httpRequest);
        return ResponseEntity.noContent().build();
    }

}