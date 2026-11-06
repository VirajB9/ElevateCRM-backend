package com.viraj.dmabackend.auth.mapper;

import com.viraj.dmabackend.auth.dto.PermissionResponse;
import com.viraj.dmabackend.auth.entity.Permission;
import com.viraj.dmabackend.auth.enums.PermissionType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PermissionMapperTest {

    private final PermissionMapper permissionMapper = new PermissionMapper();

    @Test
    void toPermissionResponse_ReturnsNullWhenPermissionIsNull() {
        assertNull(permissionMapper.toPermissionResponse(null));
    }

    @Test
    void toPermissionResponse_MapsFieldsCorrectly() {
        Permission permission = Permission.builder()
                .id("p-1")
                .permissionType(PermissionType.CLIENT_CREATE)
                .module("CLIENT")
                .description("Create client")
                .build();

        PermissionResponse response = permissionMapper.toPermissionResponse(permission);

        assertNotNull(response);
        assertEquals("p-1", response.getId());
        assertEquals("CLIENT_CREATE", response.getName());
        assertEquals("CLIENT", response.getModule());
        assertEquals("Create client", response.getDescription());
    }

    @Test
    void toPermissionResponse_HandlesNullPermissionType() {
        Permission permission = Permission.builder()
                .id("p-2")
                .permissionType(null)
                .description("No type")
                .build();

        PermissionResponse response = permissionMapper.toPermissionResponse(permission);

        assertNotNull(response);
        assertEquals("p-2", response.getId());
        assertNull(response.getName());
        assertEquals("No type", response.getDescription());
    }
}
