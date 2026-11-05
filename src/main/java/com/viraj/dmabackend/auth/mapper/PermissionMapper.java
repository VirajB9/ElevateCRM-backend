package com.viraj.dmabackend.auth.mapper;

import com.viraj.dmabackend.auth.dto.PermissionResponse;
import com.viraj.dmabackend.auth.entity.Permission;
import org.springframework.stereotype.Component;

@Component
public class PermissionMapper {

    public PermissionResponse toPermissionResponse(Permission permission) {

        if (permission == null) {
            return null;
        }

        return PermissionResponse.builder()
                .id(permission.getId())
                .name(permission.getPermissionType() != null ? permission.getPermissionType().name() : null)
                .description(permission.getDescription())
                .build();
    }
}
