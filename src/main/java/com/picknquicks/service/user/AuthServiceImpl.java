package com.picknquicks.service.user.impl;

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
import com.picknquicks.service.notification.EmailService;
import com.picknquicks.service.user.AuthService;
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

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Validate
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        // Get customer role
        Role customerRole = roleRepository.findByName("CUSTOMER")
                .orElseThrow(() -> new RuntimeException("Customer role not found"));

        // Create user
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .provider(AuthenticationType.DATABASE)
                .enabled(false) // Requires email verification
                .emailVerified(false)
                .roles(Set.of(customerRole))
                .build();

        user = userRepository.save(user);

        // Create verification token
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .build();
        verificationTokenRepository.save(verificationToken);

        // Send verification email
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
            // Authenticate
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            User user = userPrincipal.getUser();

            // Check if email is verified
            if (!user.getEmailVerified()) {
                throw new BadRequestException("Please verify your email before logging in");
            }

            // Check if account is locked
            if (user.getAccountLocked()) {
                throw new BadRequestException("Your account has been locked due to too many failed login attempts");
            }

            // Reset failed login attempts
            user.resetFailedLoginAttempts();
            user.updateLastLogin();
            userRepository.save(user);

            // Generate tokens
            String accessToken = jwtTokenProvider.generateToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

            UserResponse userResponse = mapToUserResponse(user);

            log.info("User logged in successfully: {}", user.getEmail());

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .expiresIn(jwtTokenProvider.getJwtExpiration())
                    .user(userResponse)
                    .message("Login successful")
                    .build();

        } catch (BadCredentialsException e) {
            // Handle failed login attempt
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

        // Send welcome email
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

        // Delete old tokens
        verificationTokenRepository.deleteByUser(user);

        // Create new token
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .build();
        verificationTokenRepository.save(verificationToken);

        // Send email
        emailService.sendVerificationEmail(user, token);

        log.info("Verification email resent to: {}", email);

        return ApiResponse.success("Verification email sent successfully");
    }

    @Override
    @Transactional
    public ApiResponse requestPasswordReset(PasswordResetRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        // Delete old reset tokens
        passwordResetTokenRepository.deleteByUser(user);

        // Create new reset token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(1)) // 1 hour expiry
                .build();
        passwordResetTokenRepository.save(resetToken);

        // Send reset email
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
        user.resetFailedLoginAttempts(); // Reset any locked account
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

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .enabled(user.getEnabled())
                .emailVerified(user.getEmailVerified())
                .provider(user.getProvider())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }
}