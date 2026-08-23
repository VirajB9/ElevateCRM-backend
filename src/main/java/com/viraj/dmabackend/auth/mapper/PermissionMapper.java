package com.viraj.dmabackend.auth.mapper;

import com.viraj.dmabackend.auth.dto.PermissionResponse;
import com.viraj.dmabackend.auth.entity.Permission;
import org.springframework.stereotype.Component;

@Component
public class PermissionMapper {

    public PermissionResponse toPermissionResponse(Permission permission) {

        return PermissionResponse.builder()
                .id(permission.getId())
                .name(permission.getPermissionType().name())
                .description(permission.getDescription())
                .build();
    }
}
