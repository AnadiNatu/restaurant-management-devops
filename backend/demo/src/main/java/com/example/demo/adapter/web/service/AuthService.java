package com.example.demo.adapter.web.service;

import com.example.demo.adapter.web.dto.AuthDto;

public interface AuthService {
    AuthDto.AuthResponse register(AuthDto.RegisterRequest request);
    AuthDto.AuthResponse login(AuthDto.LoginRequest request);
    void forgotPassword(AuthDto.ForgotPasswordRequest request);
    void resetPassword(AuthDto.ResetPasswordRequest request);
    void changePassword(String email, AuthDto.ChangePasswordRequest request);
    AuthDto.AuthResponse refreshToken(String refreshToken);
}
