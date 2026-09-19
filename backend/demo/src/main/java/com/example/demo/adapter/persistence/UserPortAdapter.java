package com.example.demo.adapter.persistence;

import com.example.demo.adapter.persistence.mapper.UserMapper;
import com.example.demo.adapter.persistence.repository.UserRepository;
import com.example.demo.domain.model.User;
import com.example.demo.domain.port.UserPort;
import com.example.demo.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserPortAdapter implements UserPort {

    private final UserRepository repository;
    private final UserMapper mapper;

    @Override
    public User save(User user) {
        return mapper.toDomain(repository.save(mapper.toEntity(user)));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<User> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<User> findByRole(String role) {
        return repository.findByRole(role).stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        if (!repository.existsById(id)) throw new ResourceNotFoundException("User", "id", id);
        repository.deleteById(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public User updateResetToken(String email, String token) {
        var entity = repository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        entity.setResetToken(token);
        return mapper.toDomain(repository.save(entity));
    }
}
