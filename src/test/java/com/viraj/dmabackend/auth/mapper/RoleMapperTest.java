package com.viraj.dmabackend.auth.mapper;

import com.viraj.dmabackend.auth.dto.RoleResponse;
import com.viraj.dmabackend.auth.entity.Permission;
import com.viraj.dmabackend.auth.entity.Role;
import com.viraj.dmabackend.auth.enums.PermissionType;
import com.viraj.dmabackend.auth.repository.PermissionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RoleMapperTest {

    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private RoleMapper roleMapper;

    @Test
    void toRoleResponse_ReturnsNullWhenRoleIsNull() {
        assertNull(roleMapper.toRoleResponse(null));
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

        when(permissionRepository.findAllById(List.of("perm-1"))).thenReturn(List.of(permission));

        RoleResponse response = roleMapper.toRoleResponse(role);

        assertNotNull(response);
        assertEquals("role-2", response.getId());
        assertEquals(1, response.getPermissions().size());
        assertEquals("USER_READ", response.getPermissions().get(0).getName());
    }
}
