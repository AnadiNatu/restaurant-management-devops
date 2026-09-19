package com.example.demo.adapter.web.service.impl;

import com.example.demo.adapter.web.service.ActivityLogService;
import com.example.demo.domain.model.ActivityLog;
import com.example.demo.domain.model.enums.UserRole;
import com.example.demo.domain.port.ActivityLogPort;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService {

    private static final Logger log = LoggerFactory.getLogger(ActivityLogServiceImpl.class);

    private final ActivityLogPort activityLogPort;

    @Override
    @Async
    public void log(String actorEmail, UserRole actorRole, String action,
                    String entityType, String entityId, String description,
                    HttpServletRequest request) {
        try {
            String ip = resolveClientIp(request);
            ActivityLog entry = ActivityLog.builder()
                    .actorEmail(actorEmail)
                    .actorRole(actorRole)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .description(description)
                    .ipAddress(ip)
                    .build();
            activityLogPort.save(entry);
            log.debug("[ACTIVITY] Logged | actor={} | action={} | entity={}:{}", actorEmail, action, entityType, entityId);
        } catch (Exception ex) {
            log.error("[ACTIVITY] Failed to persist log | actor={} | action={} | error={}", actorEmail, action, ex.getMessage());
        }
    }

    @Override
    public List<ActivityLog> getAll() {
        return activityLogPort.findAll();
    }

    @Override
    public List<ActivityLog> getByActor(String email) {
        return activityLogPort.findByActor(email);
    }

    @Override
    public List<ActivityLog> getByEntityType(String entityType) {
        return activityLogPort.findByEntityType(entityType);
    }

    @Override
    public List<ActivityLog> getBetween(LocalDateTime from, LocalDateTime to) {
        return activityLogPort.findBetween(from, to);
    }

    private String resolveClientIp(HttpServletRequest request) {
        if (request == null) return "unknown";
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
