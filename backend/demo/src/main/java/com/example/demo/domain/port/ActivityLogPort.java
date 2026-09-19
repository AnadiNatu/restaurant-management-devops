package com.example.demo.domain.port;

import com.example.demo.domain.model.ActivityLog;

import java.time.LocalDateTime;
import java.util.List;

public interface ActivityLogPort {
    ActivityLog save(ActivityLog log);
    List<ActivityLog> findAll();
    List<ActivityLog> findByActor(String email);
    List<ActivityLog> findBetween(LocalDateTime from, LocalDateTime to);
    List<ActivityLog> findByEntityType(String entityType);
}