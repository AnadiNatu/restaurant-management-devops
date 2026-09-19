package com.example.demo.domain.port;

import com.example.demo.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface UserPort {
    User save(User user);
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id);
    List<User> findAll();
    List<User> findByRole(String role);
    void deleteById(Long id);
    boolean existsByEmail(String email);
    User updateResetToken(String email, String token);
}