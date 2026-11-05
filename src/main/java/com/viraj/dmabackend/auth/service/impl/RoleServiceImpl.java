package com.viraj.dmabackend.auth.service.impl;

import com.viraj.dmabackend.auth.dto.*;
import com.viraj.dmabackend.auth.entity.Permission;
import com.viraj.dmabackend.auth.entity.Role;
import com.viraj.dmabackend.auth.entity.User;
import com.viraj.dmabackend.auth.exception.PermissionNotFoundException;
import com.viraj.dmabackend.auth.exception.RoleNotFoundException;
import com.viraj.dmabackend.auth.exception.SystemRoleModificationException;
import com.viraj.dmabackend.auth.exception.UnauthorizedRoleAssignmentException;
import com.viraj.dmabackend.auth.mapper.RoleMapper;
import com.viraj.dmabackend.auth.mapper.UserMapper;
import com.viraj.dmabackend.auth.repository.PermissionRepository;
import com.viraj.dmabackend.auth.repository.RoleRepository;
import com.viraj.dmabackend.auth.repository.UserRepository;
import com.viraj.dmabackend.auth.security.CustomUserDetails;
import com.viraj.dmabackend.auth.service.RoleService;
import com.viraj.dmabackend.auth.validator.RoleValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    private final PermissionRepository permissionRepository;

    private final UserRepository userRepository;

    private final RoleMapper roleMapper;

    private final UserMapper userMapper;

    private final RoleValidator roleValidator;

    @Cacheable("roles")
    @Override
    public List<RoleResponse> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        
        if (roles.size() > 100) {
            log.warn("Role count exceeds expected threshold: {}", roles.size());
        }

        List<String> allPermissionIds = roles.stream()
                .map(Role::getPermissionIds)
                .filter(java.util.Objects::nonNull)
                .flatMap(List::stream)
                .distinct()
                .toList();

        java.util.Map<String, Permission> permissionsById = allPermissionIds.isEmpty()
                ? java.util.Collections.emptyMap()
                : permissionRepository.findAllById(allPermissionIds)
                        .stream()
                        .filter(java.util.Objects::nonNull)
                        .collect(Collectors.toMap(Permission::getId, java.util.function.Function.identity(), (a, b) -> a));
        
        return roles.stream()
                .map(role -> {
                    List<Permission> rolePermissions = role.getPermissionIds() != null
                            ? role.getPermissionIds().stream()
                                    .map(permissionsById::get)
                                    .filter(java.util.Objects::nonNull)
                                    .toList()
                            : null;
                    return roleMapper.toRoleResponse(role, rolePermissions);
                })
                .toList();
    }

    @Cacheable(value = "role", key = "#roleId")
    @Override
    public RoleResponse getRoleById(String roleId) {
        Role role = findRoleById(roleId);
        return mapRoleWithPermissions(role);
    }

    @CacheEvict(value = {"roles", "role"}, allEntries = true)
    @Override
    public RoleResponse updateRole(String roleId, UpdateRoleRequest request) {

        User currentUser = findCurrentUser();

        ensureOwner(currentUser);

        Role role = findRoleById(roleId);

        preventOwnerModification(role);

        roleValidator.validateDescription(request.getDescription());

        role.setDescription(request.getDescription().trim());

        Role updatedRole = roleRepository.save(role);

        return mapRoleWithPermissions(updatedRole);
    }

    @CacheEvict(value = {"roles", "role", "menuTree"}, allEntries = true)
    @Override
    public RoleResponse assignPermissions(String roleId, AssignPermissionsRequest request) {

        User currentUser = findCurrentUser();

        ensureOwner(currentUser);

        roleValidator.validatePermissionIds(request.getPermissionIds());

        Role role = findRoleById(roleId);

        preventOwnerModification(role);

        validatePermissionsExist(request.getPermissionIds());

        assignPermissionsToRole(role, request.getPermissionIds());

        Role updatedRole = roleRepository.save(role);

        return mapRoleWithPermissions(updatedRole);
    }

    @CacheEvict(value = {"roles", "role", "menuTree"}, allEntries = true)
    @Override
    public RoleResponse removePermissions(String roleId, RemovePermissionsRequest request) {

        User currentUser = findCurrentUser();

        ensureOwner(currentUser);

        roleValidator.validatePermissionIds(request.getPermissionIds());

        Role role = findRoleById(roleId);

        preventOwnerModification(role);

        removePermissionsFromRole(role, request.getPermissionIds());

        Role updatedRole = roleRepository.save(role);

        return mapRoleWithPermissions(updatedRole);
    }

    @Override
    public Page<UserResponse> getUsersByRole(String roleId, Pageable pageable) {

        Role role = findRoleById(roleId);

        return userRepository.findByRoleIdAndStatusNot(roleId, com.viraj.dmabackend.auth.enums.UserStatus.DELETED, pageable)
                .map(user -> userMapper.toUserResponse(user, role.getName()));
    }


    // =========================
    // Helper Methods
    // =========================
    private RoleResponse mapRoleWithPermissions(Role role) {
        List<Permission> permissions = (role.getPermissionIds() != null && !role.getPermissionIds().isEmpty())
                ? permissionRepository.findAllById(role.getPermissionIds())
                : java.util.Collections.emptyList();
        return roleMapper.toRoleResponse(role, permissions);
    }

    private Role findRoleById(String roleId) {

        return roleRepository.findById(roleId)
                .orElseThrow(() ->
                        new RoleNotFoundException(roleId));
    }

    private User findCurrentUser() {

        String email = com.viraj.dmabackend.auth.security.SecurityUtils.getCurrentUserEmail()
                .orElseThrow(() -> new com.viraj.dmabackend.exception.UnauthorizedException("User not found"));

        return userRepository.findByEmailAndStatusNot(email, com.viraj.dmabackend.auth.enums.UserStatus.DELETED)
                .orElseThrow(() -> new com.viraj.dmabackend.exception.UnauthorizedException("User not found"));
    }

    private void ensureOwner(User currentUser) {

        Role role = findRoleById(currentUser.getRoleId());

        if (!"OWNER".equals(role.getName())) {
            throw new UnauthorizedRoleAssignmentException("Only OWNER can manage roles.");
        }
    }

    private void preventOwnerModification(Role role) {

        if ("OWNER".equals(role.getName())) {
            throw new SystemRoleModificationException(role.getName());
        }
    }

    private void validatePermissionsExist(List<String> permissionIds) {

        Set<String> existingPermissionIds = permissionRepository
                .findAllById(permissionIds)
                .stream()
                .map(Permission::getId)
                .collect(Collectors.toSet());

        for (String permissionId : permissionIds) {

            if (!existingPermissionIds.contains(permissionId)) {
                throw new PermissionNotFoundException(permissionId);
            }
        }
    }

    private void assignPermissionsToRole(Role role, List<String> permissionIds) {

        Set<String> updatedPermissions = role.getPermissionIds() != null
                ? new HashSet<>(role.getPermissionIds())
                : new HashSet<>();

        updatedPermissions.addAll(permissionIds);

        role.setPermissionIds(
                new ArrayList<>(updatedPermissions)
        );
    }

    private void removePermissionsFromRole(Role role, List<String> permissionIds) {

        Set<String> updatedPermissions = role.getPermissionIds() != null
                ? new HashSet<>(role.getPermissionIds())
                : new HashSet<>();

        permissionIds.forEach(updatedPermissions::remove);

        role.setPermissionIds(
                new ArrayList<>(updatedPermissions)
        );
    }
}
