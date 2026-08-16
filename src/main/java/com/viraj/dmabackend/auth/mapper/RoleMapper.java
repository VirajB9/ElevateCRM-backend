package com.viraj.dmabackend.auth.mapper;

import com.viraj.dmabackend.auth.dto.PermissionResponse;
import com.viraj.dmabackend.auth.dto.RoleResponse;
import com.viraj.dmabackend.auth.entity.Role;
import com.viraj.dmabackend.auth.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RoleMapper {

    private final PermissionRepository permissionRepository;

    public RoleResponse toRoleResponse(Role role) {

        List<PermissionResponse> permissionResponses = null;

        if (role.getPermissionIds() != null && !role.getPermissionIds().isEmpty()) {
            permissionResponses = permissionRepository.findAllById(role.getPermissionIds())
                    .stream()
                    .map(p -> PermissionResponse.builder()
                            .id(p.getId())
                            .name(p.getPermissionType().name())
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