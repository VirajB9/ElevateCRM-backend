package com.viraj.dmabackend.auth.service;

import com.viraj.dmabackend.auth.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RoleService {

    List<RoleResponse> getAllRoles();

    RoleResponse getRoleById(String roleId);

    RoleResponse updateRole(String roleId, UpdateRoleRequest request);

    RoleResponse assignPermissions(String roleId, AssignPermissionsRequest request);

    RoleResponse removePermissions(String roleId, RemovePermissionsRequest request);

    Page<UserResponse> getUsersByRole(String roleId, Pageable pageable);
}
