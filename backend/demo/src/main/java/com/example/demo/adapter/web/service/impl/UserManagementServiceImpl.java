package com.example.demo.adapter.web.service.impl;

import com.example.demo.adapter.web.dto.UserManagementDto;
import com.example.demo.adapter.web.service.UserManagementService;
import com.example.demo.domain.model.User;
import com.example.demo.domain.model.enums.UserRole;
import com.example.demo.domain.port.UserPort;
import com.example.demo.exception.DuplicateResourceException;
import com.example.demo.exception.InvalidOperationException;
import com.example.demo.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {

    private static final Logger log = LoggerFactory.getLogger(UserManagementServiceImpl.class);

    private final UserPort userPort;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserManagementDto.Response createUser(UserManagementDto.CreateUserRequest request, String createdBy) {
        if (userPort.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        User user = User.builder()
                .fname(request.getFname())
                .lname(request.getLname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(request.getRole())
                .isActive(true)
                .createdBy(createdBy)
                .build();

        User saved = userPort.save(user);
        log.info("[USER_MGMT] User created | email={} | role={} | by={}", saved.getEmail(), saved.getRole(), createdBy);
        return toResponse(saved);
    }

    @Override
    public UserManagementDto.Response updateRole(Long userId, UserManagementDto.UpdateRoleRequest request, String actorEmail) {
        User user = userPort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Prevent demoting the only SUPER_ADMIN
        if (user.getRole() == UserRole.SUPER_ADMIN) {
            throw new InvalidOperationException("updateRole", "Cannot change the role of SUPER_ADMIN");
        }

        UserRole oldRole = user.getRole();
        user.setRole(request.getRole());
        User saved = userPort.save(user);

        log.info("[USER_MGMT] Role updated | userId={} | {} -> {} | by={}",
                userId, oldRole, request.getRole(), actorEmail);
        return toResponse(saved);
    }

    @Override
    public UserManagementDto.Response updateUser(Long userId, UserManagementDto.UpdateUserRequest request) {
        User user = userPort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (request.getFname() != null) user.setFname(request.getFname());
        if (request.getLname() != null) user.setLname(request.getLname());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
        if (request.getIsActive() != null) user.setIsActive(request.getIsActive());

        User saved = userPort.save(user);
        log.info("[USER_MGMT] User updated | userId={}", userId);
        return toResponse(saved);
    }

    @Override
    public UserManagementDto.Response getById(Long id) {
        return userPort.findById(id).map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    @Override
    public List<UserManagementDto.Response> getAll() {
        return userPort.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<UserManagementDto.Response> getByRole(UserRole role) {
        return userPort.findByRole(role.name()).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public void deactivate(Long userId, String actorEmail) {
        User user = userPort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        if (user.getRole() == UserRole.SUPER_ADMIN) {
            throw new InvalidOperationException("deactivate", "Cannot deactivate SUPER_ADMIN");
        }
        user.setIsActive(false);
        userPort.save(user);
        log.info("[USER_MGMT] Deactivated | userId={} | by={}", userId, actorEmail);
    }

    @Override
    public void activate(Long userId, String actorEmail) {
        User user = userPort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setIsActive(true);
        userPort.save(user);
        log.info("[USER_MGMT] Activated | userId={} | by={}", userId, actorEmail);
    }

    @Override
    public void delete(Long userId, String actorEmail) {
        User user = userPort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        if (user.getRole() == UserRole.SUPER_ADMIN) {
            throw new InvalidOperationException("delete", "Cannot delete SUPER_ADMIN");
        }
        userPort.deleteById(userId);
        log.info("[USER_MGMT] Deleted | userId={} | by={}", userId, actorEmail);
    }

    private UserManagementDto.Response toResponse(User u) {
        UserManagementDto.Response r = new UserManagementDto.Response();
        r.setId(u.getId());
        r.setFname(u.getFname());
        r.setLname(u.getLname());
        r.setEmail(u.getEmail());
        r.setPhoneNumber(u.getPhoneNumber());
        r.setRole(u.getRole());
        r.setIsActive(u.getIsActive());
        r.setProfilePicture(u.getProfilePicture());
        r.setCreatedBy(u.getCreatedBy());
        r.setCreatedAt(u.getCreatedAt());
        r.setUpdatedAt(u.getUpdatedAt());
        return r;
    }
}
