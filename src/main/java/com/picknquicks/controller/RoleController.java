package com.picknquicks.controller;

import com.picknquicks.dto.request.CreateRoleRequest;
import com.picknquicks.dto.request.UpdateRoleRequest;
import com.picknquicks.dto.response.ApiResponse;
import com.picknquicks.dto.response.RoleResponse;
import com.picknquicks.service.role.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
@Tag(name = "Roles", description = "Role management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @Operation(summary = "Get all roles")
    public ResponseEntity<ApiResponse> getAllRoles() {
        List<RoleResponse> roles = roleService.getAllRoles();
        return ResponseEntity.ok(ApiResponse.success("Roles retrieved successfully", roles));
    }

    @GetMapping("/{roleId}")
    @Operation(summary = "Get role by ID")
    public ResponseEntity<ApiResponse> getRoleById(@PathVariable UUID roleId) {
        RoleResponse role = roleService.getRoleById(roleId);
        return ResponseEntity.ok(ApiResponse.success("Role retrieved successfully", role));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Create a new role")
    public ResponseEntity<ApiResponse> createRole(@Valid @RequestBody CreateRoleRequest request) {
        RoleResponse role = roleService.createRole(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Role created successfully", role));
    }

    @PutMapping("/{roleId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Update a role (full replacement)")
    public ResponseEntity<ApiResponse> updateRole(
            @PathVariable UUID roleId,
            @Valid @RequestBody UpdateRoleRequest request
    ) {
        RoleResponse role = roleService.updateRole(roleId, request);
        return ResponseEntity.ok(ApiResponse.success("Role updated successfully", role));
    }

    @PatchMapping("/{roleId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Partially update a role")
    public ResponseEntity<ApiResponse> partialUpdateRole(
            @PathVariable UUID roleId,
            @Valid @RequestBody UpdateRoleRequest request
    ) {
        RoleResponse role = roleService.updateRole(roleId, request);
        return ResponseEntity.ok(ApiResponse.success("Role updated successfully", role));
    }

    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Delete a role")
    public ResponseEntity<ApiResponse> deleteRole(@PathVariable UUID roleId) {
        return ResponseEntity.ok(roleService.deleteRole(roleId));
    }
}

