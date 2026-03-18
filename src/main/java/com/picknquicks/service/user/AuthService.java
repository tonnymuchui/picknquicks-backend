package com.picknquicks.service.user;
import com.picknquicks.dto.request.*;
import com.picknquicks.dto.response.AuthResponse;
import com.picknquicks.dto.response.ApiResponse;
import com.picknquicks.dto.response.UserResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    ApiResponse verifyEmail(String token);
    ApiResponse resendVerificationEmail(String email);
    ApiResponse requestPasswordReset(PasswordResetRequest request);
    ApiResponse resetPassword(PasswordResetConfirmRequest request);
    AuthResponse refreshToken(String refreshToken);
    UserResponse getCurrentUser(String email);
    UserResponse updateProfile(String email, UpdateProfileRequest request);
    ApiResponse changePassword(String email, ChangePasswordRequest request);
}