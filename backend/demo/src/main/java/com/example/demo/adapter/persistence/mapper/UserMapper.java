package com.example.demo.adapter.persistence.mapper;

import com.example.demo.adapter.persistence.entity.UserEntity;
import com.example.demo.domain.model.User;
import com.example.demo.domain.model.enums.UserRole;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toDomain(UserEntity entity) {
        if (entity == null) return null;
        User user = new User();
        user.setId(entity.getId());
        user.setFname(entity.getFname());
        user.setLname(entity.getLname());
        user.setEmail(entity.getEmail());
        user.setPassword(entity.getPassword());
        user.setPhoneNumber(entity.getPhoneNumber());
        user.setRole(UserRole.valueOf(entity.getRole()));
        user.setIsActive(entity.getIsActive());
        user.setResetToken(entity.getResetToken());
        user.setProfilePicture(entity.getProfilePicture());
        user.setCreatedBy(entity.getCreatedBy());
        user.setCreatedAt(entity.getCreatedAt());
        user.setUpdatedAt(entity.getUpdatedAt());
        return user;
    }

    public UserEntity toEntity(User domain) {
        if (domain == null) return null;
        return UserEntity.builder()
                .id(domain.getId())
                .fname(domain.getFname())
                .lname(domain.getLname())
                .email(domain.getEmail())
                .password(domain.getPassword())
                .phoneNumber(domain.getPhoneNumber())
                .role(domain.getRole() != null ? domain.getRole().name() : UserRole.EMPLOYEE.name())
                .isActive(domain.getIsActive() != null ? domain.getIsActive() : true)
                .resetToken(domain.getResetToken())
                .profilePicture(domain.getProfilePicture())
                .createdBy(domain.getCreatedBy())
                .build();
    }
}
