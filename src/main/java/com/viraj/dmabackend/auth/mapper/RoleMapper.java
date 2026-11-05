package com.viraj.dmabackend.auth.mapper;

import com.viraj.dmabackend.auth.dto.PermissionResponse;
import com.viraj.dmabackend.auth.dto.RoleResponse;
import com.viraj.dmabackend.auth.entity.Permission;
import com.viraj.dmabackend.auth.entity.Role;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class RoleMapper {

    public RoleResponse toRoleResponse(Role role) {
        return toRoleResponse(role, (List<Permission>) null);
    }

    public RoleResponse toRoleResponse(Role role, List<Permission> permissions) {

        if (role == null) {
            return null;
        }

        List<PermissionResponse> permissionResponses = null;

        if (permissions != null && !permissions.isEmpty()) {
            permissionResponses = permissions.stream()
                    .filter(Objects::nonNull)
                    .map(p -> PermissionResponse.builder()
                            .id(p.getId())
                            .name(p.getPermissionType() != null ? p.getPermissionType().name() : null)
                            .description(p.getDescription())
                            .build())
                    .collect(Collectors.toList());
        }

        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .systemRole(role.isSystemRole())
                .permissions(permissionResponses)
                .build();
    }
}

