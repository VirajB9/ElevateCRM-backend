package com.viraj.dmabackend.auth.controller;

import com.viraj.dmabackend.auth.dto.*;
import com.viraj.dmabackend.auth.service.impl.RoleServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "3. Role Management")
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class RoleController {

    private final RoleServiceImpl roleService;

    @PreAuthorize("hasAuthority('role:read')")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get All Roles")
    public List<RoleResponse> getAllRoles() {

        return roleService.getAllRoles();
    }

    @PreAuthorize("hasAuthority('role:read')")
    @GetMapping("/{roleId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get Role By Id")
    public RoleResponse getRoleById(
            @PathVariable String roleId) {

        return roleService.getRoleById(roleId);
    }

    @PreAuthorize("hasAuthority('role:update')")
    @PutMapping("/{roleId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update Role")
    public RoleResponse updateRole(
            @PathVariable String roleId,
            @Valid @RequestBody UpdateRoleRequest request) {

        return roleService.updateRole(roleId, request);
    }

    @PreAuthorize("hasAuthority('role:update')")
    @PatchMapping("/{roleId}/permissions")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Assign Permissions To Role")
    public RoleResponse assignPermissions(
            @PathVariable String roleId,
            @Valid @RequestBody AssignPermissionsRequest request) {

        return roleService.assignPermissions(roleId, request);
    }

    @PreAuthorize("hasAuthority('role:update')")
    @DeleteMapping("/{roleId}/permissions")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Remove Permissions From Role")
    public RoleResponse removePermissions(
            @PathVariable String roleId,
            @Valid @RequestBody RemovePermissionsRequest request) {

        return roleService.removePermissions(roleId, request);
    }

    @PreAuthorize("hasAuthority('role:read')")
    @GetMapping("/{roleId}/users")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get Users By Role")
    public Page<UserResponse> getUsersByRole(
            @PathVariable String roleId,
            Pageable pageable) {

        return roleService.getUsersByRole(roleId, pageable);
    }
}