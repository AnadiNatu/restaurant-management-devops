package com.example.demo.adapter.persistence;

import com.example.demo.adapter.persistence.mapper.MenuItemMapper;
import com.example.demo.adapter.persistence.repository.MenuItemRepository;
import com.example.demo.domain.model.MenuItem;
import com.example.demo.domain.port.MenuItemPort;
import com.example.demo.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MenuItemPortAdapter implements MenuItemPort {


    private final MenuItemRepository repository;
    private final MenuItemMapper mapper;

    @Override
    public MenuItem save(MenuItem item) {
        return mapper.toDomain(repository.save(mapper.toEntity(item)));
    }

    @Override
    public Optional<MenuItem> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<MenuItem> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<MenuItem> findByCategory(String category) {
        return repository.findByCategory(category).stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<MenuItem> findByIsAvailable(Boolean available) {
        return repository.findByIsAvailable(available).stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        if (!repository.existsById(id)) throw new ResourceNotFoundException("MenuItem", "id", id);
        repository.deleteById(id);
    }
}
