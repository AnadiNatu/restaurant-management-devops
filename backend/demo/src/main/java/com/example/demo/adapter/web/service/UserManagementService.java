package com.example.demo.adapter.web.service;

import com.example.demo.adapter.web.dto.UserManagementDto;
import com.example.demo.domain.model.enums.UserRole;

import java.util.List;

public interface UserManagementService {

    UserManagementDto.Response createUser(UserManagementDto.CreateUserRequest request, String createdBy);
    UserManagementDto.Response updateRole(Long userId, UserManagementDto.UpdateRoleRequest request, String actorEmail);
    UserManagementDto.Response updateUser(Long userId, UserManagementDto.UpdateUserRequest request);
    UserManagementDto.Response getById(Long id);
    List<UserManagementDto.Response> getAll();
    List<UserManagementDto.Response> getByRole(UserRole role);
    void deactivate(Long userId, String actorEmail);
    void activate(Long userId, String actorEmail);
    void delete(Long userId, String actorEmail);

}
