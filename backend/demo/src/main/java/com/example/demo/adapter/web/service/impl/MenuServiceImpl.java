package com.example.demo.adapter.web.service.impl;

import com.example.demo.adapter.web.dto.MenuItemDto;
import com.example.demo.adapter.web.service.MenuService;
import com.example.demo.domain.model.MenuItem;
import com.example.demo.domain.port.MenuItemPort;
import com.example.demo.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private static final Logger log = LoggerFactory.getLogger(MenuServiceImpl.class);

    private final MenuItemPort menuItemPort;

    @Override
    @CacheEvict(value = {"menuItems", "menuByCategory"}, allEntries = true)
    @Transactional
    public MenuItemDto.Response create(MenuItemDto.CreateRequest request, String createdBy) {
        MenuItem item = new MenuItem();
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        item.setCategory(request.getCategory());
        item.setIsAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true);
        item.setImageUrl(request.getImageUrl());
        item.setTaxCategory(request.getTaxCategory());
        item.setTaxRate(request.getTaxRate() != null ? request.getTaxRate() : BigDecimal.ZERO);
        item.setCreatedBy(createdBy);
        item.setUpdatedBy(createdBy);

        MenuItem saved = menuItemPort.save(item);
        log.info("[MENU] Item created | name={} | by={}", saved.getName(), createdBy);
        return toResponse(saved);
    }

    @Override
    @CacheEvict(value = {"menuItems", "menuByCategory"}, allEntries = true)
    @Transactional
    public MenuItemDto.Response update(Long id, MenuItemDto.UpdateRequest request, String updatedBy) {
        MenuItem item = menuItemPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", id));

        if (request.getName() != null) item.setName(request.getName());
        if (request.getDescription() != null) item.setDescription(request.getDescription());
        if (request.getPrice() != null) item.setPrice(request.getPrice());
        if (request.getCategory() != null) item.setCategory(request.getCategory());
        if (request.getIsAvailable() != null) item.setIsAvailable(request.getIsAvailable());
        if (request.getImageUrl() != null) item.setImageUrl(request.getImageUrl());
        if (request.getTaxCategory() != null) item.setTaxCategory(request.getTaxCategory());
        if (request.getTaxRate() != null) item.setTaxRate(request.getTaxRate());
        item.setUpdatedBy(updatedBy);

        MenuItem saved = menuItemPort.save(item);
        log.info("[MENU] Item updated | id={} | by={}", id, updatedBy);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public MenuItemDto.Response getById(Long id) {
        return menuItemPort.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", id));
    }

    @Override
    @Cacheable("menuItems")
    @Transactional(readOnly = true)
    public List<MenuItemDto.Response> getAll() {
        return menuItemPort.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "menuByCategory", key = "#category")
    @Transactional(readOnly = true)
    public List<MenuItemDto.Response> getByCategory(String category) {
        return menuItemPort.findByCategory(category).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemDto.Response> getAvailable() {
        return menuItemPort.findByIsAvailable(true).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = {"menuItems", "menuByCategory"}, allEntries = true)
    @Transactional
    public MenuItemDto.Response toggleAvailability(Long id, String updatedBy) {
        MenuItem item = menuItemPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", id));
        item.setIsAvailable(!Boolean.TRUE.equals(item.getIsAvailable()));
        item.setUpdatedBy(updatedBy);
        MenuItem saved = menuItemPort.save(item);
        log.info("[MENU] Availability toggled | id={} | available={} | by={}", id, saved.getIsAvailable(), updatedBy);
        return toResponse(saved);
    }

    @Override
    @CacheEvict(value = {"menuItems", "menuByCategory"}, allEntries = true)
    @Transactional
    public void delete(Long id) {
        menuItemPort.deleteById(id);
        log.info("[MENU] Item deleted | id={}", id);
    }

    private MenuItemDto.Response toResponse(MenuItem item) {
        MenuItemDto.Response r = new MenuItemDto.Response();
        r.setId(item.getId());
        r.setName(item.getName());
        r.setDescription(item.getDescription());
        r.setPrice(item.getPrice());
        r.setCategory(item.getCategory());
        r.setIsAvailable(item.getIsAvailable());
        r.setImageUrl(item.getImageUrl());
        r.setTaxCategory(item.getTaxCategory());
        r.setTaxRate(item.getTaxRate());
        r.setCreatedAt(item.getCreatedAt());
        r.setUpdatedAt(item.getUpdatedAt());
        r.setCreatedBy(item.getCreatedBy());
        r.setUpdatedBy(item.getUpdatedBy());
        return r;
    }
}