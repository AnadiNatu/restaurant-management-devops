package com.example.demo.notification_email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final OtpGenerator otpGenerator;
    private final OtpStore otpStore;

    public void sendEmail(String to, String subject, String body, boolean isHtml) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("noreply@restaurant.com");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, isHtml);
            mailSender.send(message);
            log.info("[EMAIL] Sent | to={} | subject={}", to, subject);
        } catch (MessagingException ex) {
            log.error("[EMAIL] Failed | to={} | error={}", to, ex.getMessage());
            throw new RuntimeException("Email delivery failed: " + ex.getMessage());
        }
    }

    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        sendEmail(to, subject, htmlBody, true);
    }

    public void sendSimpleEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("[EMAIL] Simple email sent | to={}", to);
        } catch (Exception ex) {
            log.error("[EMAIL] Simple email failed | to={} | error={}", to, ex.getMessage());
            throw new RuntimeException("Email delivery failed: " + ex.getMessage());
        }
    }

    public void sendOtpViaEmail(String email) {
        String otp = otpGenerator.generate();
        otpStore.save(email, otp);
        String body = buildOtpBody(otp);
        sendEmail(email, "Your OTP Code", body, true);
        log.info("[EMAIL] OTP sent | to={}", email);
    }

    public boolean validateOtp(String email, String otp) {
        boolean valid = otpStore.validate(email, otp);
        log.info("[OTP] Validation | email={} | valid={}", email, valid);
        return valid;
    }

    public void sendWelcomeEmail(String to, String firstName) {
        String subject = "Welcome to the Restaurant System";
        String body = "<div style='font-family:Arial,sans-serif;max-width:520px;margin:auto'>"
                + "<h2 style='color:#e53935'>Welcome, " + firstName + "!</h2>"
                + "<p>Your account has been successfully created.</p>"
                + "<p>You can now log in and start managing your restaurant operations.</p>"
                + "<br/><p style='color:#888'>Restaurant Management System</p>"
                + "</div>";
        sendEmail(to, subject, body, true);
    }

    private String buildOtpBody(String otp) {
        return "<div style='font-family:Arial,sans-serif;max-width:480px;margin:auto'>"
                + "<h2>OTP Verification</h2>"
                + "<p>Use the following One-Time Password to verify your identity:</p>"
                + "<h1 style='letter-spacing:8px;color:#e53935'>" + otp + "</h1>"
                + "<p>This OTP is valid for <strong>5 minutes</strong>. Do not share it with anyone.</p>"
                + "</div>";
    }

}
