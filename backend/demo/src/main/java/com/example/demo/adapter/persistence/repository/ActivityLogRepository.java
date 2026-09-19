package com.example.demo.adapter.persistence.repository;

import com.example.demo.adapter.persistence.entity.ActivityLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLogEntity , Long> {

    List<ActivityLogEntity> findByActorEmailOrderByTimestampDesc(String email);
    List<ActivityLogEntity> findByEntityTypeOrderByTimestampDesc(String entityType);

    @Query("SELECT a FROM ActivityLogEntity a WHERE a.timestamp BETWEEN :from AND :to ORDER BY a.timestamp DESC")
    List<ActivityLogEntity> findBetween(LocalDateTime from, LocalDateTime to);

}
