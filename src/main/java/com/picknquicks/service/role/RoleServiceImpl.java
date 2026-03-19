package com.picknquicks.service.role;

import com.picknquicks.domain.user.Role;
import com.picknquicks.dto.request.CreateRoleRequest;
import com.picknquicks.dto.request.UpdateRoleRequest;
import com.picknquicks.dto.response.ApiResponse;
import com.picknquicks.dto.response.RoleResponse;
import com.picknquicks.exception.BadRequestException;
import com.picknquicks.exception.ResourceNotFoundException;
import com.picknquicks.mapper.RoleMapper;
import com.picknquicks.repository.user.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    private static final Set<String> PROTECTED_ROLES = Set.of(
            Role.ADMIN, Role.CUSTOMER, Role.STAFF, Role.MANAGER
    );

    @Override
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(roleMapper::toRoleResponse)
                .toList();
    }

    @Override
    public RoleResponse getRoleById(UUID roleId) {
        Role role = findRoleOrThrow(roleId);
        return roleMapper.toRoleResponse(role);
    }

    @Override
    @Transactional
    public RoleResponse createRole(CreateRoleRequest request) {
        String normalizedName = request.getName().toUpperCase().trim();

        if (roleRepository.existsByName(normalizedName)) {
            throw new BadRequestException("Role '%s' already exists".formatted(normalizedName));
        }

        Role role = Role.builder()
                .name(normalizedName)
                .description(request.getDescription())
                .build();

        Role saved = roleRepository.save(role);
        log.info("Role created: {}", saved.getName());
        return roleMapper.toRoleResponse(saved);
    }

    @Override
    @Transactional
    public RoleResponse updateRole(UUID roleId, UpdateRoleRequest request) {
        Role role = findRoleOrThrow(roleId);

        if (request.getName() != null) {
            String normalizedName = request.getName().toUpperCase().trim();

            if (PROTECTED_ROLES.contains(role.getName()) && !role.getName().equals(normalizedName)) {
                throw new BadRequestException("Cannot rename protected role '%s'".formatted(role.getName()));
            }

            if (!role.getName().equals(normalizedName) && roleRepository.existsByName(normalizedName)) {
                throw new BadRequestException("Role '%s' already exists".formatted(normalizedName));
            }

            role.setName(normalizedName);
        }

        if (request.getDescription() != null) {
            role.setDescription(request.getDescription());
        }

        Role updated = roleRepository.save(role);
        log.info("Role updated: {}", updated.getName());
        return roleMapper.toRoleResponse(updated);
    }

    @Override
    @Transactional
    public ApiResponse deleteRole(UUID roleId) {
        Role role = findRoleOrThrow(roleId);

        if (PROTECTED_ROLES.contains(role.getName())) {
            throw new BadRequestException("Cannot delete protected role '%s'".formatted(role.getName()));
        }

        if (!role.getUsers().isEmpty()) {
            throw new BadRequestException(
                    "Cannot delete role '%s' — it is assigned to %d user(s)".formatted(role.getName(), role.getUsers().size())
            );
        }

        roleRepository.delete(role);
        log.info("Role deleted: {}", role.getName());
        return ApiResponse.success("Role '%s' deleted successfully".formatted(role.getName()));
    }

    private Role findRoleOrThrow(UUID roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));
    }
}

