package com.example.demo.adapter.persistence.mapper;

import com.example.demo.adapter.persistence.entity.ActivityLogEntity;
import com.example.demo.domain.model.ActivityLog;
import com.example.demo.domain.model.enums.UserRole;
import org.springframework.stereotype.Component;

@Component
public class ActivityLogMapper {

    public ActivityLog toDomain(ActivityLogEntity entity) {
        if (entity == null) return null;
        ActivityLog log = new ActivityLog();
        log.setId(entity.getId());
        log.setActorEmail(entity.getActorEmail());
        log.setActorRole(UserRole.valueOf(entity.getActorRole()));
        log.setAction(entity.getAction());
        log.setEntityType(entity.getEntityType());
        log.setEntityId(entity.getEntityId());
        log.setDescription(entity.getDescription());
        log.setIpAddress(entity.getIpAddress());
        log.setTimestamp(entity.getTimestamp());
        return log;
    }

    public ActivityLogEntity toEntity(ActivityLog domain) {
        if (domain == null) return null;
        return ActivityLogEntity.builder()
                .actorEmail(domain.getActorEmail())
                .actorRole(domain.getActorRole() != null ? domain.getActorRole().name() : "EMPLOYEE")
                .action(domain.getAction())
                .entityType(domain.getEntityType())
                .entityId(domain.getEntityId())
                .description(domain.getDescription())
                .ipAddress(domain.getIpAddress())
                .build();
    }
}
