package com.example.demo.adapter.web.controller;

import com.example.demo.adapter.web.dto.AuthDto;
import com.example.demo.notification_email.EmailService;
import com.example.demo.notification_email.OtpStore;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/otp")
@RequiredArgsConstructor
public class OtpController {

    private final EmailService emailService;
    private final OtpStore otpStore;

    @PostMapping("/send")
    public ResponseEntity<String> sendOtp(@RequestParam String email) {
        emailService.sendOtpViaEmail(email);
        return ResponseEntity.ok("OTP sent to " + email);
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyOtp(@Valid @RequestBody AuthDto.OtpVerifyRequest request) {
        boolean valid = emailService.validateOtp(request.getEmail(), request.getOtp());
        if (valid) {
            return ResponseEntity.ok("OTP verified successfully.");
        }
        return ResponseEntity.badRequest().body("Invalid or expired OTP.");
    }

}
