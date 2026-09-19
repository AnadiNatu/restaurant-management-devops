package com.example.demo.adapter.web.service.impl;

import com.example.demo.adapter.security.JwtUtil;
import com.example.demo.adapter.security.RestaurantUserDetails;
import com.example.demo.adapter.security.RestaurantUserDetailsService;
import com.example.demo.adapter.web.dto.AuthDto;
import com.example.demo.adapter.web.service.AuthService;
import com.example.demo.domain.model.User;
import com.example.demo.domain.model.enums.UserRole;
import com.example.demo.domain.port.UserPort;
import com.example.demo.exception.DuplicateResourceException;
import com.example.demo.exception.InvalidOperationException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.notification_email.EmailService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserPort userPort;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final RestaurantUserDetailsService userDetailsService;
    private final EmailService emailService;

    @Override
    public AuthDto.AuthResponse register(AuthDto.RegisterRequest request) {
        if (userPort.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        User user = User.builder()
                .fname(request.getFname())
                .lname(request.getLname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(UserRole.EMPLOYEE)   // Default role; Super Admin grants ADMIN
                .isActive(true)
                .build();

        User saved = userPort.save(user);
        log.info("[AUTH] New user registered | email={} | role=EMPLOYEE", saved.getEmail());

        var userDetails = new RestaurantUserDetails(saved);
        String token = jwtUtil.generateToken(userDetails);
        String refresh = jwtUtil.generateRefreshToken(saved.getEmail());

        emailService.sendWelcomeEmail(saved.getEmail(), saved.getFname());

        return new AuthDto.AuthResponse(token, refresh, saved.getEmail(),
                saved.getFname() + " " + saved.getLname(), saved.getRole().name(), saved.getId());
    }

    @Override
    public AuthDto.AuthResponse login(AuthDto.LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        var userDetails = (RestaurantUserDetails) userDetailsService.loadUserByUsername(request.getEmail());
        User user = userDetails.getUser();

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new InvalidOperationException("login", "Account is deactivated. Contact your administrator.");
        }

        String token = jwtUtil.generateToken(userDetails);
        String refresh = jwtUtil.generateRefreshToken(user.getEmail());

        log.info("[AUTH] Login successful | email={} | role={}", user.getEmail(), user.getRole());
        return new AuthDto.AuthResponse(token, refresh, user.getEmail(),
                user.getFname() + " " + user.getLname(), user.getRole().name(), user.getId());
    }

    @Override
    public void forgotPassword(AuthDto.ForgotPasswordRequest request) {
        User user = userPort.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        String token = UUID.randomUUID().toString();
        userPort.updateResetToken(user.getEmail(), token);

        String resetLink = "http://localhost:5173/reset-password?token=" + token;
        emailService.sendHtmlEmail(user.getEmail(), "Password Reset Request",
                buildResetEmailBody(user.getFname(), resetLink));

        log.info("[AUTH] Password reset email sent | email={}", user.getEmail());
    }

    @Override
    public void resetPassword(AuthDto.ResetPasswordRequest request) {
        User user = userPort.findByEmail(
                        userPort.findAll().stream()
                                .filter(u -> request.getToken().equals(u.getResetToken()))
                                .findFirst()
                                .map(User::getEmail)
                                .orElseThrow(() -> new InvalidOperationException("resetPassword", "Invalid or expired token"))
                )
                .orElseThrow(() -> new InvalidOperationException("resetPassword", "User not found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetToken(null);
        userPort.save(user);
        log.info("[AUTH] Password reset successful | email={}", user.getEmail());
    }

    @Override
    public void changePassword(String email, AuthDto.ChangePasswordRequest request) {
        User user = userPort.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidOperationException("changePassword", "Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userPort.save(user);
        log.info("[AUTH] Password changed | email={}", email);
    }

    @Override
    public AuthDto.AuthResponse refreshToken(String refreshToken) {
        String username = jwtUtil.extractUsername(refreshToken);
        String type = jwtUtil.extractClaim(refreshToken, c -> c.get("type", String.class));

        if (!"refresh".equals(type)) {
            throw new InvalidOperationException("refreshToken", "Not a valid refresh token");
        }

        var userDetails = (RestaurantUserDetails) userDetailsService.loadUserByUsername(username);
        User user = userDetails.getUser();
        String newToken = jwtUtil.generateToken(userDetails);
        String newRefresh = jwtUtil.generateRefreshToken(username);

        return new AuthDto.AuthResponse(newToken, newRefresh, user.getEmail(),
                user.getFname() + " " + user.getLname(), user.getRole().name(), user.getId());
    }

    private String buildResetEmailBody(String name, String link) {
        return "<div style='font-family:Arial,sans-serif;max-width:520px;margin:auto'>"
                + "<h2>Password Reset</h2>"
                + "<p>Hi " + name + ",</p>"
                + "<p>Click the button below to reset your password. This link expires in 1 hour.</p>"
                + "<a href='" + link + "' style='background:#e53935;color:white;padding:12px 24px;"
                + "text-decoration:none;border-radius:4px;display:inline-block'>Reset Password</a>"
                + "<p style='margin-top:20px;color:#666'>If you did not request this, ignore this email.</p>"
                + "</div>";
    }
}
