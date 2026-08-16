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
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    private final PermissionRepository permissionRepository;

    private final UserRepository userRepository;

    private final RoleMapper roleMapper;

    private final UserMapper userMapper;

    private final RoleValidator roleValidator;

    @Override
    public List<RoleResponse> getAllRoles() {

        return roleRepository.findAll()
                .stream()
                .map(roleMapper::toRoleResponse)
                .toList();
    }

    @Override
    public RoleResponse getRoleById(String roleId) {

        Role role = findRoleById(roleId);

        return roleMapper.toRoleResponse(role);
    }

    @Override
    public RoleResponse updateRole(String roleId, UpdateRoleRequest request) {

        Role role = findRoleById(roleId);

        roleValidator.validateDescription(request.getDescription());

        role.setDescription(request.getDescription().trim());

        Role updatedRole = roleRepository.save(role);

        return roleMapper.toRoleResponse(updatedRole);
    }

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

        return roleMapper.toRoleResponse(updatedRole);
    }

    @Override
    public RoleResponse removePermissions(String roleId, RemovePermissionsRequest request) {

        User currentUser = findCurrentUser();

        ensureOwner(currentUser);

        roleValidator.validatePermissionIds(request.getPermissionIds());

        Role role = findRoleById(roleId);

        preventOwnerModification(role);

        removePermissionsFromRole(role, request.getPermissionIds());

        Role updatedRole = roleRepository.save(role);

        return roleMapper.toRoleResponse(updatedRole);
    }

    @Override
    public Page<UserResponse> getUsersByRole(String roleId, Pageable pageable) {

        findRoleById(roleId);

        return userRepository.findByRoleId(roleId, pageable)
                .map(userMapper::toUserResponse);
    }


    // =========================
    // Helper Methods
    // =========================
    private Role findRoleById(String roleId) {

        return roleRepository.findById(roleId)
                .orElseThrow(() ->
                        new RoleNotFoundException(roleId));
    }

    private User findCurrentUser() {

        CustomUserDetails userDetails =
                (CustomUserDetails) SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal();

        return userDetails.getUser();
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

        Set<String> updatedPermissions =
                new HashSet<>(role.getPermissionIds());

        updatedPermissions.addAll(permissionIds);

        role.setPermissionIds(
                new ArrayList<>(updatedPermissions)
        );
    }

    private void removePermissionsFromRole(Role role, List<String> permissionIds) {

        Set<String> updatedPermissions =
                new HashSet<>(role.getPermissionIds());

        permissionIds.forEach(updatedPermissions::remove);

        role.setPermissionIds(
                new ArrayList<>(updatedPermissions)
        );
    }
}