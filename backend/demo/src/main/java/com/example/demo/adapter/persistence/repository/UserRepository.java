package com.example.demo.adapter.persistence.repository;

import com.example.demo.adapter.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity , Long>{
    Optional<UserEntity> findByEmail(String email);
    List<UserEntity> findByRole(String role);
    boolean existsByEmail(String email);
    Optional<UserEntity> findByResetToken(String resetToken);
}
