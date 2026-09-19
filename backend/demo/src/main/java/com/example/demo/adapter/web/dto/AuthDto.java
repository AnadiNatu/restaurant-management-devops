package com.example.demo.adapter.web.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class AuthDto {

    @Data
    public static class RegisterRequest {
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
    }

    @Data
    public static class LoginRequest {
        @Email
        @NotBlank
        private String email;

        @NotBlank
        private String password;
    }

    @Data
    public static class ForgotPasswordRequest {
        @Email
        @NotBlank
        private String email;
    }

    @Data
    public static class ResetPasswordRequest {
        @NotBlank
        private String token;

        @NotBlank
        @Size(min = 8)
        private String newPassword;
    }

    @Data
    public static class ChangePasswordRequest {
        @NotBlank
        private String currentPassword;

        @NotBlank
        @Size(min = 8)
        private String newPassword;
    }

    @Data
    public static class OtpVerifyRequest {
        @Email
        @NotBlank
        private String email;

        @NotBlank
        private String otp;
    }

    @Data
    public static class AuthResponse {
        private String token;
        private String refreshToken;
        private String email;
        private String fullName;
        private String role;
        private Long userId;

        public AuthResponse(String token, String refreshToken, String email,
                            String fullName, String role, Long userId) {
            this.token = token;
            this.refreshToken = refreshToken;
            this.email = email;
            this.fullName = fullName;
            this.role = role;
            this.userId = userId;
        }
    }
}
