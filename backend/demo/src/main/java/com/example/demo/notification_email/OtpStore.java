package com.example.demo.notification_email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OtpStore {

    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCK_MINUTES = 10;

    @Value("${otp.expiry-minutes:5}")
    private int expiryMinutes;

    private record OtpEntry(String otp, LocalDateTime expireAt, int attempts, LocalDateTime lockedUntil) {}

    private final Map<String, OtpEntry> store = new ConcurrentHashMap<>();

    public void save(String key, String otp) {
        store.put(key, new OtpEntry(otp, LocalDateTime.now().plusMinutes(expiryMinutes), 0, null));
    }

    public boolean validate(String key, String otp) {
        OtpEntry entry = store.get(key);
        if (entry == null) return false;

        LocalDateTime now = LocalDateTime.now();

        if (entry.lockedUntil() != null && now.isBefore(entry.lockedUntil())) return false;

        if (now.isAfter(entry.expireAt())) {
            store.remove(key);
            return false;
        }

        if (entry.otp().equals(otp)) {
            store.remove(key);
            return true;
        }

        int newAttempts = entry.attempts() + 1;
        LocalDateTime lockUntil = newAttempts >= MAX_ATTEMPTS
                ? now.plusMinutes(LOCK_MINUTES) : null;
        store.put(key, new OtpEntry(entry.otp(), entry.expireAt(), newAttempts, lockUntil));
        return false;
    }

    public void invalidate(String key) {
        store.remove(key);
    }

}