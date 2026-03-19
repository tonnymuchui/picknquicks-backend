package com.picknquicks.controller;
import com.picknquicks.dto.request.CreateStaffRequest;
import com.picknquicks.dto.request.UpdateUserRolesRequest;
import com.picknquicks.dto.request.UpdateUserStatusRequest;
import com.picknquicks.dto.request.UserSearchRequest;
import com.picknquicks.dto.response.ApiResponse;
import com.picknquicks.dto.response.DashboardStatsResponse;
import com.picknquicks.dto.response.PaginatedResponse;
import com.picknquicks.dto.response.UserResponse;
import com.picknquicks.service.admin.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Get all users", description = "Get paginated list of all users with optional filters")
    public ResponseEntity<PaginatedResponse<UserResponse>> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean emailVerified,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        UserSearchRequest request = UserSearchRequest.builder()
                .search(search)
                .role(role)
                .emailVerified(emailVerified)
                .enabled(enabled)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        return ResponseEntity.ok(adminService.getAllUsers(request));
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID userId) {
        return ResponseEntity.ok(adminService.getUserById(userId));
    }

    @PutMapping("/users/{userId}/roles")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Update user roles")
    public ResponseEntity<UserResponse> updateUserRoles(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRolesRequest request
    ) {
        return ResponseEntity.ok(adminService.updateUserRoles(userId, request));
    }

    @PostMapping("/users/staff")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Create staff member")
    public ResponseEntity<ApiResponse> createStaff(@Valid @RequestBody CreateStaffRequest request) {
        return ResponseEntity.ok(adminService.createStaff(request));
    }

    @PatchMapping("/users/{userId}/status")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Enable or disable user")
    public ResponseEntity<ApiResponse> updateUserStatus(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {
        return ResponseEntity.ok(adminService.updateUserStatus(userId, request));
    }

    @DeleteMapping("/users/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Delete user")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(adminService.deleteUser(userId));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'STAFF')")
    @Operation(summary = "Get dashboard statistics")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }
}