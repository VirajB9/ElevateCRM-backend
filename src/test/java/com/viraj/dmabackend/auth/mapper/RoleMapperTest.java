package com.viraj.dmabackend.auth.mapper;

import com.viraj.dmabackend.auth.dto.RoleResponse;
import com.viraj.dmabackend.auth.entity.Permission;
import com.viraj.dmabackend.auth.entity.Role;
import com.viraj.dmabackend.auth.enums.PermissionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RoleMapperTest {

    private RoleMapper roleMapper;

    @BeforeEach
    void setUp() {
        roleMapper = new RoleMapper();
    }

    @Test
    void toRoleResponse_ReturnsNullWhenRoleIsNull() {
        assertNull(roleMapper.toRoleResponse(null));
        assertNull(roleMapper.toRoleResponse(null, List.of()));
    }

    @Test
    void toRoleResponse_MapsRoleWithoutPermissions() {
        Role role = Role.builder()
                .id("role-1")
                .name("MANAGER")
                .description("Manager Role")
                .systemRole(false)
                .permissionIds(null)
                .build();

        RoleResponse response = roleMapper.toRoleResponse(role);

        assertNotNull(response);
        assertEquals("role-1", response.getId());
        assertEquals("MANAGER", response.getName());
        assertEquals("Manager Role", response.getDescription());
        assertFalse(response.isSystemRole());
        assertNull(response.getPermissions());
    }

    @Test
    void toRoleResponse_MapsRoleWithPermissions() {
        Role role = Role.builder()
                .id("role-2")
                .name("ADMIN")
                .description("Admin Role")
                .systemRole(true)
                .permissionIds(List.of("perm-1"))
                .build();

        Permission permission = Permission.builder()
                .id("perm-1")
                .permissionType(PermissionType.USER_READ)
                .description("Read user")
                .build();

        RoleResponse response = roleMapper.toRoleResponse(role, List.of(permission));

        assertNotNull(response);
        assertEquals("role-2", response.getId());
        assertNotNull(response.getPermissions());
        assertEquals(1, response.getPermissions().size());
        assertEquals("USER_READ", response.getPermissions().get(0).getName());
    }
}

