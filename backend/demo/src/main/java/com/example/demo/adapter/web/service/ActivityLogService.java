package com.example.demo.adapter.web.service;

import com.example.demo.domain.model.ActivityLog;
import com.example.demo.domain.model.enums.UserRole;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.List;

public interface ActivityLogService {
    void log(String actorEmail, UserRole actorRole, String action,
             String entityType, String entityId, String description, HttpServletRequest request);
    List<ActivityLog> getAll();
    List<ActivityLog> getByActor(String email);
    List<ActivityLog> getByEntityType(String entityType);
    List<ActivityLog> getBetween(LocalDateTime from, LocalDateTime to);
}
