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
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserManagementService userManagementService;
    private final ActivityLogService activityLogService;

    @PostMapping("/employees")
    public ResponseEntity<UserManagementDto.Response> createEmployee(@Valid @RequestBody UserManagementDto.CreateUserRequest request, @AuthenticationPrincipal RestaurantUserDetails principal, HttpServletRequest httpRequest) {

        // Admins can only create EMPLOYEEs
        request.setRole(UserRole.EMPLOYEE);
        String actor = principal.getUsername();
        UserManagementDto.Response response = userManagementService.createUser(request, actor);
        activityLogService.log(actor, principal.getUser().getRole(),
                "CREATE_EMPLOYEE", "USER", String.valueOf(response.getId()),
                "Employee created: " + response.getEmail(), httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/employees")
    public ResponseEntity<List<UserManagementDto.Response>> getEmployees() {
        return ResponseEntity.ok(userManagementService.getByRole(UserRole.EMPLOYEE));
    }

    @GetMapping("/employees/{id}")
    public ResponseEntity<UserManagementDto.Response> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(userManagementService.getById(id));
    }

    @PutMapping("/employees/{id}")
    public ResponseEntity<UserManagementDto.Response> updateEmployee(@PathVariable Long id, @Valid @RequestBody UserManagementDto.UpdateUserRequest request) {
        return ResponseEntity.ok(userManagementService.updateUser(id, request));
    }

    @PatchMapping("/employees/{id}/activate")
    public ResponseEntity<String> activate(@PathVariable Long id, @AuthenticationPrincipal RestaurantUserDetails principal, HttpServletRequest httpRequest) {

        String actor = principal.getUsername();
        userManagementService.activate(id, actor);
        activityLogService.log(actor, principal.getUser().getRole(),
                "ACTIVATE_EMPLOYEE", "USER", String.valueOf(id), "Employee activated", httpRequest);
        return ResponseEntity.ok("Employee activated.");
    }

    @PatchMapping("/employees/{id}/deactivate")
    public ResponseEntity<String> deactivate(@PathVariable Long id, @AuthenticationPrincipal RestaurantUserDetails principal, HttpServletRequest httpRequest) {

        String actor = principal.getUsername();
        userManagementService.deactivate(id, actor);
        activityLogService.log(actor, principal.getUser().getRole(),
                "DEACTIVATE_EMPLOYEE", "USER", String.valueOf(id), "Employee deactivated", httpRequest);
        return ResponseEntity.ok("Employee deactivated.");
    }

    @DeleteMapping("/employees/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id,
            @AuthenticationPrincipal RestaurantUserDetails principal,
            HttpServletRequest httpRequest) {

        String actor = principal.getUsername();
        userManagementService.delete(id, actor);
        activityLogService.log(actor, principal.getUser().getRole(),
                "DELETE_EMPLOYEE", "USER", String.valueOf(id), "Employee deleted", httpRequest);
        return ResponseEntity.noContent().build();
    }

}
