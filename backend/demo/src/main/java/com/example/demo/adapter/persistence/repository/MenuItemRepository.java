package com.example.demo.adapter.persistence.repository;

import com.example.demo.adapter.persistence.entity.MenuItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItemEntity , Long> {

    List<MenuItemEntity> findByCategory(String category);
    List<MenuItemEntity> findByIsAvailable(Boolean isAvailable);
    List<MenuItemEntity> findByCategoryAndIsAvailable(String category, Boolean isAvailable);
}
