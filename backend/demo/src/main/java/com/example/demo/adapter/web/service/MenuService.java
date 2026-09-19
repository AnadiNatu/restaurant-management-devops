package com.example.demo.adapter.web.service;

import com.example.demo.adapter.web.dto.MenuItemDto;

import java.util.List;

public interface MenuService {
    MenuItemDto.Response create(MenuItemDto.CreateRequest request, String createdBy);
    MenuItemDto.Response update(Long id, MenuItemDto.UpdateRequest request, String updatedBy);
    MenuItemDto.Response getById(Long id);
    List<MenuItemDto.Response> getAll();
    List<MenuItemDto.Response> getByCategory(String category);
    List<MenuItemDto.Response> getAvailable();
    MenuItemDto.Response toggleAvailability(Long id, String updatedBy);
    void delete(Long id);
}
