package com.picknquicks.service.admin;

import com.picknquicks.domain.user.Role;
import com.picknquicks.domain.user.User;
import com.picknquicks.dto.request.CreateStaffRequest;
import com.picknquicks.dto.request.UpdateUserRolesRequest;
import com.picknquicks.dto.request.UpdateUserStatusRequest;
import com.picknquicks.dto.request.UserSearchRequest;
import com.picknquicks.dto.response.ApiResponse;
import com.picknquicks.dto.response.DashboardStatsResponse;
import com.picknquicks.dto.response.PaginatedResponse;
import com.picknquicks.dto.response.UserResponse;
import com.picknquicks.exception.BadRequestException;
import com.picknquicks.exception.ResourceNotFoundException;
import com.picknquicks.mapper.UserMapper;
import com.picknquicks.repository.user.RoleRepository;
import com.picknquicks.repository.user.UserRepository;
import com.picknquicks.security.AuthenticationType;
import com.picknquicks.service.notification.EmailService;
import com.picknquicks.util.PagingAndSortingHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final UserMapper userMapper;
    private final PagingAndSortingHelper pagingHelper;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
    private static final int TEMP_PASSWORD_LENGTH = 12;

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<UserResponse> getAllUsers(UserSearchRequest request) {
        Pageable pageable = pagingHelper.buildPageable(
                request.getPage(), request.getSize(), request.getSortBy(), request.getSortDirection()
        );

        Page<User> userPage;

        if (request.getSearch() != null && !request.getSearch().isEmpty()) {
            userPage = userRepository.search(request.getSearch(), pageable);
        } else if (request.getRole() != null && !request.getRole().isEmpty()) {
            userPage = userRepository.findByRoleName(request.getRole(), pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        return pagingHelper.toPaginatedResponse(userPage, userMapper::toUserResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUserRoles(UUID userId, UpdateUserRolesRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (request.getRoles() == null || request.getRoles().isEmpty()) {
            throw new BadRequestException("At least one role is required");
        }

        Set<Role> roles = new HashSet<>();
        for (String roleName : request.getRoles()) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new BadRequestException("Invalid role: " + roleName));
            roles.add(role);
        }

        user.setRoles(roles);
        User updatedUser = userRepository.save(user);

        log.info("Updated roles for user: {} to {}", user.getEmail(), request.getRoles());

        return userMapper.toUserResponse(updatedUser);
    }

    @Override
    @Transactional
    public ApiResponse createStaff(CreateStaffRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        if (request.getRoles() == null || request.getRoles().isEmpty()) {
            throw new BadRequestException("At least one role is required");
        }

        Set<Role> roles = new HashSet<>();
        for (String roleName : request.getRoles()) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new BadRequestException("Invalid role: " + roleName));
            roles.add(role);
        }

        String tempPassword = generateTemporaryPassword();

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(tempPassword))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .provider(AuthenticationType.DATABASE)
                .enabled(true)
                .emailVerified(true)
                .roles(roles)
                .build();

        User savedUser = userRepository.save(user);

        emailService.sendStaffWelcomeEmail(savedUser, tempPassword);

        log.info("Created staff member: {} with roles {}", savedUser.getEmail(), request.getRoles());

        return ApiResponse.success("Staff member created successfully", userMapper.toUserResponse(savedUser));
    }

    @Override
    @Transactional
    public ApiResponse updateUserStatus(UUID userId, UpdateUserStatusRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setEnabled(request.getEnabled());

        if (!request.getEnabled()) {
            user.resetFailedLoginAttempts();
        }

        userRepository.save(user);

        String status = request.getEnabled() ? "enabled" : "disabled";
        log.info("Updated user status: {} to {}", user.getEmail(), status);

        return ApiResponse.success("User " + status + " successfully");
    }

    @Override
    @Transactional
    public ApiResponse deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.isAdmin()) {
            long adminCount = userRepository.countByRoleName("ADMIN");
            if (adminCount <= 1) {
                throw new BadRequestException("Cannot delete the last admin user");
            }
        }

        userRepository.delete(user);

        log.info("Deleted user: {}", user.getEmail());

        return ApiResponse.success("User deleted successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        return DashboardStatsResponse.builder()
                .totalUsers(userRepository.count())
                .totalCustomers(userRepository.countByRoleName("CUSTOMER"))
                .activeUsers(userRepository.countActiveUsers())
                .totalOrders(0L)
                .totalRevenue(null)
                .pendingOrders(0L)
                .completedOrders(0L)
                .totalProducts(0L)
                .lowStockProducts(0L)
                .outOfStockProducts(0L)
                .build();
    }


    private String generateTemporaryPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(TEMP_PASSWORD_LENGTH);

        for (int i = 0; i < TEMP_PASSWORD_LENGTH; i++) {
            password.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }

        return password.toString();
    }
}