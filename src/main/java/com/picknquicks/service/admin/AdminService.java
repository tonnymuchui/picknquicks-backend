package com.picknquicks.service.admin;

import com.picknquicks.dto.request.CreateStaffRequest;
import com.picknquicks.dto.request.UpdateUserRolesRequest;
import com.picknquicks.dto.request.UpdateUserStatusRequest;
import com.picknquicks.dto.request.UserSearchRequest;
import com.picknquicks.dto.response.ApiResponse;
import com.picknquicks.dto.response.DashboardStatsResponse;
import com.picknquicks.dto.response.PaginatedResponse;
import com.picknquicks.dto.response.UserResponse;

import java.util.UUID;

public interface AdminService {

    PaginatedResponse<UserResponse> getAllUsers(UserSearchRequest request);
    UserResponse getUserById(UUID userId);
    UserResponse updateUserRoles(UUID userId, UpdateUserRolesRequest request);
    ApiResponse createStaff(CreateStaffRequest request);
    ApiResponse updateUserStatus(UUID userId, UpdateUserStatusRequest request);
    ApiResponse deleteUser(UUID userId);
    DashboardStatsResponse getDashboardStats();
}