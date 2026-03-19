package com.picknquicks.service.user;

import com.picknquicks.domain.user.*;
import com.picknquicks.dto.request.*;
import com.picknquicks.dto.response.ApiResponse;
import com.picknquicks.dto.response.AuthResponse;
import com.picknquicks.dto.response.UserResponse;
import com.picknquicks.exception.BadRequestException;
import com.picknquicks.exception.ResourceNotFoundException;
import com.picknquicks.repository.user.*;
import com.picknquicks.security.AuthenticationType;
import com.picknquicks.security.JwtTokenProvider;
import com.picknquicks.security.UserPrincipal;
import com.picknquicks.mapper.UserMapper;
import com.picknquicks.service.notification.EmailService;
import com.picknquicks.service.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;
    private final UserMapper userMapper;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        Role customerRole = roleRepository.findByName("CUSTOMER")
                .orElseThrow(() -> new RuntimeException("Customer role not found"));

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .provider(AuthenticationType.DATABASE)
                .enabled(false)
                .emailVerified(false)
                .roles(Set.of(customerRole))
                .build();

        user = userRepository.save(user);

        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .build();
        verificationTokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(user, token);

        log.info("User registered successfully: {}", user.getEmail());

        return AuthResponse.builder()
                .message("Registration successful! Please check your email to verify your account.")
                .build();
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            User user = userPrincipal.getUser();

            if (!user.getEmailVerified()) {
                throw new BadRequestException("Please verify your email before logging in");
            }

            if (user.getAccountLocked()) {
                throw new BadRequestException("Your account has been locked due to too many failed login attempts");
            }

            user.resetFailedLoginAttempts();
            user.updateLastLogin();
            userRepository.save(user);

            String accessToken = jwtTokenProvider.generateToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

            UserResponse userResponse = userMapper.toUserResponse(user);

            log.info("User logged in successfully: {}", user.getEmail());

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .expiresIn(jwtTokenProvider.getJwtExpiration())
                    .user(userResponse)
                    .message("Login successful")
                    .build();

        } catch (BadCredentialsException e) {
            User user = userRepository.findByEmail(request.getEmail()).orElse(null);
            if (user != null) {
                user.incrementFailedLoginAttempts();
                userRepository.save(user);
            }
            throw new BadRequestException("Invalid email or password");
        }
    }

    @Override
    @Transactional
    public ApiResponse verifyEmail(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid verification token"));

        if (verificationToken.isExpired()) {
            throw new BadRequestException("Verification token has expired");
        }

        if (verificationToken.getVerified()) {
            throw new BadRequestException("Email already verified");
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        user.setEnabled(true);
        userRepository.save(user);

        verificationToken.setVerified(true);
        verificationTokenRepository.save(verificationToken);

        emailService.sendWelcomeEmail(user);

        log.info("Email verified for user: {}", user.getEmail());

        return ApiResponse.success("Email verified successfully! You can now log in.");
    }

    @Override
    @Transactional
    public ApiResponse resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (user.getEmailVerified()) {
            throw new BadRequestException("Email already verified");
        }

        verificationTokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .build();
        verificationTokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(user, token);

        log.info("Verification email resent to: {}", email);

        return ApiResponse.success("Verification email sent successfully");
    }

    @Override
    @Transactional
    public ApiResponse requestPasswordReset(PasswordResetRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        passwordResetTokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(1))
                .build();
        passwordResetTokenRepository.save(resetToken);

        emailService.sendPasswordResetEmail(user, token);

        log.info("Password reset requested for: {}", user.getEmail());

        return ApiResponse.success("Password reset email sent successfully");
    }

    @Override
    @Transactional
    public ApiResponse resetPassword(PasswordResetConfirmRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid reset token"));

        if (!resetToken.isValid()) {
            throw new BadRequestException("Reset token is invalid or has been used");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.resetFailedLoginAttempts();
        userRepository.save(user);

        resetToken.markAsUsed();
        passwordResetTokenRepository.save(resetToken);

        log.info("Password reset successfully for: {}", user.getEmail());

        return ApiResponse.success("Password reset successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BadRequestException("Invalid refresh token");
        }

        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserPrincipal userPrincipal = new UserPrincipal(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, userPrincipal.getAuthorities()
        );

        String newAccessToken = jwtTokenProvider.generateToken(authentication);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtTokenProvider.getJwtExpiration())
                .message("Token refreshed successfully")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            user.setLastName(request.getLastName());
        }

        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            user.setPhone(request.getPhone());
        }

        if (request.getAvatarFile() != null && !request.getAvatarFile().isEmpty()) {
            String oldAvatarUrl = user.getAvatarUrl();
            if (oldAvatarUrl != null && !oldAvatarUrl.isBlank()) {
                try {
                    log.info("Removing old avatar: {}", oldAvatarUrl);
                    fileStorageService.deleteFile(oldAvatarUrl);
                } catch (Exception e) {
                    log.warn("Could not delete old avatar file: {}", oldAvatarUrl, e);
                }
            }

            try {
                String avatarFilePath = fileStorageService.storeFile(request.getAvatarFile(), "avatar");
                user.setAvatarUrl(avatarFilePath);
                log.info("Avatar updated for user: {}", email);
            } catch (IOException e) {
                log.error("Failed to upload avatar for user: {}", email, e);
                throw new BadRequestException("Failed to upload avatar: " + e.getMessage());
            }
        }

        user = userRepository.save(user);

        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public ApiResponse changePassword(String email, ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New passwords do not match");
        }

        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            throw new BadRequestException("New password must be different from current password");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", email);

        return ApiResponse.success("Password changed successfully");
    }
}