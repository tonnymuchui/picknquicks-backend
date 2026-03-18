package com.picknquicks.mapper;

import com.picknquicks.domain.user.Role;
import com.picknquicks.dto.response.RoleResponse;
import org.springframework.stereotype.Component;

@Component
public class RoleMapper {

    public RoleResponse toRoleResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .userCount(role.getUsers() != null ? role.getUsers().size() : 0)
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }
}

