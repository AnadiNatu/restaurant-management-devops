package com.example.demo.adapter.web.controller;

import com.example.demo.adapter.security.RestaurantUserDetails;
import com.example.demo.adapter.web.dto.UserManagementDto;
import com.example.demo.adapter.web.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserManagementService userManagementService;

    @GetMapping
    public ResponseEntity<UserManagementDto.Response> getProfile(
            @AuthenticationPrincipal RestaurantUserDetails principal) {
        return ResponseEntity.ok(userManagementService.getById(principal.getUser().getId()));
    }

    @PutMapping
    public ResponseEntity<UserManagementDto.Response> updateProfile(
            @AuthenticationPrincipal RestaurantUserDetails principal,
            @RequestBody UserManagementDto.UpdateUserRequest request) {
        // Prevent is_active from being self-modified
        request.setIsActive(null);
        return ResponseEntity.ok(userManagementService.updateUser(principal.getUser().getId(), request));
    }
}
