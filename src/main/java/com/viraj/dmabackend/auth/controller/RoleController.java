package com.viraj.dmabackend.auth.controller;

import com.viraj.dmabackend.auth.dto.*;
import com.viraj.dmabackend.auth.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

@Tag(name = "3. Role Management", description = "Endpoints for managing roles and their associated permissions")
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class RoleController {

    private final RoleService roleService;

    @PreAuthorize("hasAuthority('role:read')")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get All Roles", description = "Retrieves a complete list of all roles in the system. Requires 'role:read' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Roles retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public List<RoleResponse> getAllRoles() {
        return roleService.getAllRoles();
    }

    @PreAuthorize("hasAuthority('role:read')")
    @GetMapping("/{roleId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get Role By Id", description = "Retrieves a specific role's details by its ID. Requires 'role:read' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Role found and returned"),
            @ApiResponse(responseCode = "404", description = "Role not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public RoleResponse getRoleById(
            @Parameter(description = "Unique identifier of the role", required = true)
            @PathVariable String roleId) {
        return roleService.getRoleById(roleId);
    }

    @PreAuthorize("hasAuthority('role:update')")
    @PutMapping("/{roleId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update Role", description = "Updates a role's basic details (e.g., name, description). Requires 'role:update' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Role updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Role not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public RoleResponse updateRole(
            @Parameter(description = "Unique identifier of the role", required = true) @PathVariable String roleId,
            @Valid @RequestBody UpdateRoleRequest request) {
        return roleService.updateRole(roleId, request);
    }

    @PreAuthorize("hasAuthority('role:update')")
    @PatchMapping("/{roleId}/permissions")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Assign Permissions To Role", description = "Grants additional permissions to a specific role. Requires 'role:update' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Permissions assigned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid permission format or data"),
            @ApiResponse(responseCode = "404", description = "Role not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public RoleResponse assignPermissions(
            @Parameter(description = "Unique identifier of the role", required = true) @PathVariable String roleId,
            @Valid @RequestBody AssignPermissionsRequest request) {
        return roleService.assignPermissions(roleId, request);
    }

    @PreAuthorize("hasAuthority('role:update')")
    @DeleteMapping("/{roleId}/permissions")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Remove Permissions From Role", description = "Revokes specific permissions from a role. Requires 'role:update' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Permissions removed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid permission format or data"),
            @ApiResponse(responseCode = "404", description = "Role not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public RoleResponse removePermissions(
            @Parameter(description = "Unique identifier of the role", required = true) @PathVariable String roleId,
            @Valid @RequestBody RemovePermissionsRequest request) {
        return roleService.removePermissions(roleId, request);
    }

    @PreAuthorize("hasAuthority('role:read')")
    @GetMapping("/{roleId}/users")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get Users By Role", description = "Retrieves a paginated list of all users assigned to this role. Requires 'role:read' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Role not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public Page<UserResponse> getUsersByRole(
            @Parameter(description = "Unique identifier of the role", required = true) @PathVariable String roleId,
            @Parameter(description = "Pagination parameters (page, size, sort)") Pageable pageable) {
        return roleService.getUsersByRole(roleId, pageable);
    }
}
