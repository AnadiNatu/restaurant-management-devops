package com.example.demo.adapter.persistence;

import com.example.demo.adapter.persistence.mapper.ActivityLogMapper;
import com.example.demo.adapter.persistence.repository.ActivityLogRepository;
import com.example.demo.domain.model.ActivityLog;
import com.example.demo.domain.port.ActivityLogPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ActivityLogAdapter implements ActivityLogPort {


    private final ActivityLogRepository repository;
    private final ActivityLogMapper mapper;

    @Override
    public ActivityLog save(ActivityLog log) {
        return mapper.toDomain(repository.save(mapper.toEntity(log)));
    }

    @Override
    public List<ActivityLog> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<ActivityLog> findByActor(String email) {
        return repository.findByActorEmailOrderByTimestampDesc(email).stream()
                .map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<ActivityLog> findBetween(LocalDateTime from, LocalDateTime to) {
        return repository.findBetween(from, to).stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<ActivityLog> findByEntityType(String entityType) {
        return repository.findByEntityTypeOrderByTimestampDesc(entityType).stream()
                .map(mapper::toDomain).collect(Collectors.toList());
    }

}
