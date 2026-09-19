package com.example.demo.domain.port;

import com.example.demo.domain.model.MenuItem;

import java.util.List;
import java.util.Optional;

public interface MenuItemPort {
    MenuItem save(MenuItem item);
    Optional<MenuItem> findById(Long id);
    List<MenuItem> findAll();
    List<MenuItem> findByCategory(String category);
    List<MenuItem> findByIsAvailable(Boolean available);
    void deleteById(Long id);
}
