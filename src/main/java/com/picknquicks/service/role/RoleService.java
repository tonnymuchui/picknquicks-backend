package com.picknquicks.service.role;

import com.picknquicks.dto.request.CreateRoleRequest;
import com.picknquicks.dto.request.UpdateRoleRequest;
import com.picknquicks.dto.response.ApiResponse;
import com.picknquicks.dto.response.RoleResponse;

import java.util.List;
import java.util.UUID;

public interface RoleService {

    List<RoleResponse> getAllRoles();

    RoleResponse getRoleById(UUID roleId);

    RoleResponse createRole(CreateRoleRequest request);

    RoleResponse updateRole(UUID roleId, UpdateRoleRequest request);

    ApiResponse deleteRole(UUID roleId);
}

