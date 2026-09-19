package com.example.demo.adapter.web.controller;

import com.example.demo.adapter.web.service.ActivityLogService;
import com.example.demo.domain.model.ActivityLog;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    @GetMapping
    public ResponseEntity<List<ActivityLog>> getAll() {
        return ResponseEntity.ok(activityLogService.getAll());
    }

    @GetMapping("/actor/{email}")
    public ResponseEntity<List<ActivityLog>> getByActor(@PathVariable String email) {
        return ResponseEntity.ok(activityLogService.getByActor(email));
    }

    @GetMapping("/entity/{entityType}")
    public ResponseEntity<List<ActivityLog>> getByEntityType(@PathVariable String entityType) {
        return ResponseEntity.ok(activityLogService.getByEntityType(entityType));
    }

    @GetMapping("/between")
    public ResponseEntity<List<ActivityLog>> getBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(activityLogService.getBetween(from, to));
    }

}