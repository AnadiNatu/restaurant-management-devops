package com.example.demo.adapter.web.dto;


import com.example.demo.domain.model.enums.UserRole;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

public class UserManagementDto {

    @Data
    public static class CreateUserRequest {
        @NotBlank
        private String fname;

        @NotBlank
        private String lname;

        @Email
        @NotBlank
        private String email;

        @NotBlank
        @Size(min = 8)
        private String password;

        private String phoneNumber;

        @NotNull
        private UserRole role;
    }

    @Data
    public static class UpdateRoleRequest {
        @NotNull
        private UserRole role;
    }

    @Data
    public static class UpdateUserRequest {
        private String fname;
        private String lname;
        private String phoneNumber;
        private Boolean isActive;
    }

    @Data
    public static class Response {
        private Long id;
        private String fname;
        private String lname;
        private String email;
        private String phoneNumber;
        private UserRole role;
        private Boolean isActive;
        private String profilePicture;
        private String createdBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
