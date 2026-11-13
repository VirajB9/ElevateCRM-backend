package com.viraj.dmabackend.auth.service.impl;

import com.viraj.dmabackend.auth.dto.UserResponse;
import com.viraj.dmabackend.auth.entity.Permission;
import com.viraj.dmabackend.auth.entity.Role;
import com.viraj.dmabackend.auth.entity.User;
import com.viraj.dmabackend.auth.enums.UserStatus;
import com.viraj.dmabackend.auth.mapper.RoleMapper;
import com.viraj.dmabackend.auth.mapper.UserMapper;
import com.viraj.dmabackend.auth.repository.PermissionRepository;
import com.viraj.dmabackend.auth.repository.RoleRepository;
import com.viraj.dmabackend.auth.repository.UserRepository;
import com.viraj.dmabackend.auth.validator.RoleValidator;
import com.viraj.dmabackend.exception.BadRequestException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RoleValidator roleValidator;

    @InjectMocks
    private RoleServiceImpl roleService;

    private final Pageable pageable = PageRequest.of(0, 10);

    @AfterEach
    void tearDown() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void getUsersByRole_ExcludesDeletedUsers() {
        Role role = Role.builder().id("role-1").name("EMPLOYEE").build();
        User user = User.builder().id("user-1").email("emp@agency.com").roleId("role-1").status(UserStatus.ACTIVE).build();

        when(roleRepository.findById("role-1")).thenReturn(Optional.of(role));
        when(userRepository.findByRoleIdAndStatusNot(eq("role-1"), eq(UserStatus.DELETED), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(user)));
        when(userMapper.toUserResponse(any(User.class), eq("EMPLOYEE")))
                .thenReturn(UserResponse.builder().id("user-1").email("emp@agency.com").roleName("EMPLOYEE").build());

        Page<UserResponse> response = roleService.getUsersByRole("role-1", pageable);

        assertEquals(1, response.getTotalElements());
        verify(userRepository).findByRoleIdAndStatusNot(eq("role-1"), eq(UserStatus.DELETED), eq(pageable));
    }

    @Test
    void assignPermissions_whenRoleHasNullPermissionIds_assignsSuccessfullyWithoutNPE() {
        Role ownerRole = Role.builder().id("owner-role").name("OWNER").build();
        User currentUser = User.builder().id("owner-user").email("owner@test.com").roleId("owner-role").build();
        Role targetRole = Role.builder().id("role-2").name("MANAGER").permissionIds(null).build();
        com.viraj.dmabackend.auth.entity.Permission permission =
                com.viraj.dmabackend.auth.entity.Permission.builder().id("p1").build();

        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("owner@test.com", "pass"));

        when(userRepository.findByEmailAndStatusNot("owner@test.com", UserStatus.DELETED))
                .thenReturn(Optional.of(currentUser));
        when(roleRepository.findById("owner-role")).thenReturn(Optional.of(ownerRole));
        when(roleRepository.findById("role-2")).thenReturn(Optional.of(targetRole));
        when(permissionRepository.findAllById(List.of("p1"))).thenReturn(List.of(permission));
        when(roleRepository.save(any(Role.class))).thenReturn(targetRole);
        when(roleMapper.toRoleResponse(any(), any())).thenReturn(com.viraj.dmabackend.auth.dto.RoleResponse.builder().build());

        com.viraj.dmabackend.auth.dto.AssignPermissionsRequest request = new com.viraj.dmabackend.auth.dto.AssignPermissionsRequest();
        request.setPermissionIds(List.of("p1"));

        roleService.assignPermissions("role-2", request);

        verify(roleRepository).save(targetRole);
        org.assertj.core.api.Assertions.assertThat(targetRole.getPermissionIds()).containsExactly("p1");
    }

    @Test
    void removePermissions_whenRoleHasNullPermissionIds_executesSuccessfullyWithoutNPE() {
        Role ownerRole = Role.builder().id("owner-role").name("OWNER").build();
        User currentUser = User.builder().id("owner-user").email("owner@test.com").roleId("owner-role").build();
        Role targetRole = Role.builder().id("role-2").name("MANAGER").permissionIds(null).build();

        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("owner@test.com", "pass"));

        when(userRepository.findByEmailAndStatusNot("owner@test.com", UserStatus.DELETED))
                .thenReturn(Optional.of(currentUser));
        when(roleRepository.findById("owner-role")).thenReturn(Optional.of(ownerRole));
        when(roleRepository.findById("role-2")).thenReturn(Optional.of(targetRole));
        when(roleRepository.save(any(Role.class))).thenReturn(targetRole);
        when(roleMapper.toRoleResponse(any(), any())).thenReturn(com.viraj.dmabackend.auth.dto.RoleResponse.builder().build());

        com.viraj.dmabackend.auth.dto.RemovePermissionsRequest request = new com.viraj.dmabackend.auth.dto.RemovePermissionsRequest();
        request.setPermissionIds(List.of("p1"));

        roleService.removePermissions("role-2", request);

        verify(roleRepository).save(targetRole);
        org.assertj.core.api.Assertions.assertThat(targetRole.getPermissionIds()).isEmpty();
    }

    @Test
    void assignPermissions_TrimsSurroundingWhitespaceFromPermissionIds() {
        Role ownerRole = Role.builder().id("owner-role").name("OWNER").build();
        User currentUser = User.builder().id("owner-user").email("owner@test.com").roleId("owner-role").build();
        Role targetRole = Role.builder().id("role-2").name("MANAGER").permissionIds(new java.util.ArrayList<>()).build();
        com.viraj.dmabackend.auth.entity.Permission permission =
                com.viraj.dmabackend.auth.entity.Permission.builder().id("p1").build();

        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("owner@test.com", "pass"));

        when(userRepository.findByEmailAndStatusNot("owner@test.com", UserStatus.DELETED))
                .thenReturn(Optional.of(currentUser));
        when(roleRepository.findById("owner-role")).thenReturn(Optional.of(ownerRole));
        when(roleRepository.findById("role-2")).thenReturn(Optional.of(targetRole));
        when(permissionRepository.findAllById(List.of("p1"))).thenReturn(List.of(permission));
        when(roleRepository.save(any(Role.class))).thenReturn(targetRole);
        when(roleMapper.toRoleResponse(any(), any())).thenReturn(com.viraj.dmabackend.auth.dto.RoleResponse.builder().build());

        com.viraj.dmabackend.auth.dto.AssignPermissionsRequest request = new com.viraj.dmabackend.auth.dto.AssignPermissionsRequest();
        request.setPermissionIds(List.of("   p1   "));

        roleService.assignPermissions("role-2", request);

        verify(permissionRepository, times(2)).findAllById(List.of("p1"));
        org.assertj.core.api.Assertions.assertThat(targetRole.getPermissionIds()).containsExactly("p1");
    }

    @Test
    void removePermissions_TrimsSurroundingWhitespaceFromPermissionIds() {
        Role ownerRole = Role.builder().id("owner-role").name("OWNER").build();
        User currentUser = User.builder().id("owner-user").email("owner@test.com").roleId("owner-role").build();
        Role targetRole = Role.builder().id("role-2").name("MANAGER").permissionIds(new java.util.ArrayList<>(List.of("p1"))).build();

        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("owner@test.com", "pass"));

        when(userRepository.findByEmailAndStatusNot("owner@test.com", UserStatus.DELETED))
                .thenReturn(Optional.of(currentUser));
        when(roleRepository.findById("owner-role")).thenReturn(Optional.of(ownerRole));
        when(roleRepository.findById("role-2")).thenReturn(Optional.of(targetRole));
        when(roleRepository.save(any(Role.class))).thenReturn(targetRole);
        when(roleMapper.toRoleResponse(any(), any())).thenReturn(com.viraj.dmabackend.auth.dto.RoleResponse.builder().build());

        com.viraj.dmabackend.auth.dto.RemovePermissionsRequest request = new com.viraj.dmabackend.auth.dto.RemovePermissionsRequest();
        request.setPermissionIds(List.of("   p1   "));

        roleService.removePermissions("role-2", request);

        verify(roleRepository).save(targetRole);
        org.assertj.core.api.Assertions.assertThat(targetRole.getPermissionIds()).isEmpty();
    }

    @Test
    void updateRole_ThrowsWhenCallerIsNotOwner() {
        Role managerRole = Role.builder().id("mgr-role").name("MANAGER").build();
        User currentUser = User.builder().id("mgr-user").email("mgr@test.com").roleId("mgr-role").build();

        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("mgr@test.com", "pass"));

        when(userRepository.findByEmailAndStatusNot("mgr@test.com", UserStatus.DELETED))
                .thenReturn(Optional.of(currentUser));
        when(roleRepository.findById("mgr-role")).thenReturn(Optional.of(managerRole));

        com.viraj.dmabackend.auth.dto.UpdateRoleRequest request = new com.viraj.dmabackend.auth.dto.UpdateRoleRequest();
        request.setDescription("New desc");

        org.junit.jupiter.api.Assertions.assertThrows(
                com.viraj.dmabackend.auth.exception.UnauthorizedRoleAssignmentException.class,
                () -> roleService.updateRole("any-role", request)
        );
    }

    @Test
    void updateRole_ThrowsWhenTargetRoleIsOwner() {
        Role ownerRole = Role.builder().id("owner-role").name("OWNER").build();
        User currentUser = User.builder().id("owner-user").email("owner@test.com").roleId("owner-role").build();

        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("owner@test.com", "pass"));

        when(userRepository.findByEmailAndStatusNot("owner@test.com", UserStatus.DELETED))
                .thenReturn(Optional.of(currentUser));
        when(roleRepository.findById("owner-role")).thenReturn(Optional.of(ownerRole));

        com.viraj.dmabackend.auth.dto.UpdateRoleRequest request = new com.viraj.dmabackend.auth.dto.UpdateRoleRequest();
        request.setDescription("New desc");

        org.junit.jupiter.api.Assertions.assertThrows(
                com.viraj.dmabackend.auth.exception.SystemRoleModificationException.class,
                () -> roleService.updateRole("owner-role", request)
        );
    }

    @Test
    void updateRole_SucceedsWhenCallerIsOwnerAndTargetIsNotOwner() {
        Role ownerRole = Role.builder().id("owner-role").name("OWNER").build();
        User currentUser = User.builder().id("owner-user").email("owner@test.com").roleId("owner-role").build();
        Role targetRole = Role.builder().id("role-target").name("EMPLOYEE").description("Old desc").build();

        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("owner@test.com", "pass"));

        when(userRepository.findByEmailAndStatusNot("owner@test.com", UserStatus.DELETED))
                .thenReturn(Optional.of(currentUser));
        when(roleRepository.findById("owner-role")).thenReturn(Optional.of(ownerRole));
        when(roleRepository.findById("role-target")).thenReturn(Optional.of(targetRole));
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roleMapper.toRoleResponse(any(), any())).thenReturn(com.viraj.dmabackend.auth.dto.RoleResponse.builder().build());

        com.viraj.dmabackend.auth.dto.UpdateRoleRequest request = new com.viraj.dmabackend.auth.dto.UpdateRoleRequest();
        request.setDescription("  Updated Employee description  ");

        roleService.updateRole("role-target", request);

        verify(roleValidator).validateDescription("  Updated Employee description  ");
        verify(roleRepository).save(targetRole);
        assertEquals("Updated Employee description", targetRole.getDescription());
    }

    @Test
    void updateRole_ThrowsOptimisticLockingFailure_WhenVersionMismatch() {
        Role ownerRole = Role.builder().id("owner-role").name("OWNER").build();
        User currentUser = User.builder().id("owner-user").email("owner@test.com").roleId("owner-role").build();
        Role targetRole = Role.builder().id("role-target").name("EMPLOYEE").description("Old desc").build();
        targetRole.setVersion(2L);

        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("owner@test.com", "pass"));

        when(userRepository.findByEmailAndStatusNot("owner@test.com", UserStatus.DELETED))
                .thenReturn(Optional.of(currentUser));
        when(roleRepository.findById("owner-role")).thenReturn(Optional.of(ownerRole));
        when(roleRepository.findById("role-target")).thenReturn(Optional.of(targetRole));

        com.viraj.dmabackend.auth.dto.UpdateRoleRequest request = new com.viraj.dmabackend.auth.dto.UpdateRoleRequest();
        request.setVersion(1L);

        assertThrows(org.springframework.dao.OptimisticLockingFailureException.class,
                () -> roleService.updateRole("role-target", request));
    }

    @Test
    void getAllRoles_FetchesPermissionsInBulkSingleQuery() {
        Role role1 = Role.builder().id("r1").name("MGR").permissionIds(List.of("p1", "p2")).build();
        Role role2 = Role.builder().id("r2").name("EMP").permissionIds(List.of("p2", "p3")).build();

        Permission p1 = Permission.builder().id("p1").build();
        Permission p2 = Permission.builder().id("p2").build();
        Permission p3 = Permission.builder().id("p3").build();

        when(roleRepository.findAll()).thenReturn(List.of(role1, role2));
        when(permissionRepository.findAllById(List.of("p1", "p2", "p3"))).thenReturn(List.of(p1, p2, p3));
        when(roleMapper.toRoleResponse(any(), any())).thenReturn(com.viraj.dmabackend.auth.dto.RoleResponse.builder().build());

        List<com.viraj.dmabackend.auth.dto.RoleResponse> responses = roleService.getAllRoles();

        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(permissionRepository, times(1)).findAllById(List.of("p1", "p2", "p3"));
        verify(roleMapper, times(2)).toRoleResponse(any(), any());
    }

    @Test
    void getRoleById_TrimsRoleId() {
        Role role = Role.builder().id("role-1").name("MANAGER").build();
        when(roleRepository.findById("role-1")).thenReturn(Optional.of(role));
        when(roleMapper.toRoleResponse(any(), any())).thenReturn(com.viraj.dmabackend.auth.dto.RoleResponse.builder().id("role-1").build());

        com.viraj.dmabackend.auth.dto.RoleResponse response = roleService.getRoleById("   role-1   ");

        assertNotNull(response);
        verify(roleRepository).findById("role-1");
    }

    @Test
    void removePermissions_ThrowsWhenRemovingAllPermissionsFromSystemRole() {
        Role ownerRole = Role.builder().id("owner-role").name("OWNER").build();
        User currentUser = User.builder().id("owner-user").email("owner@test.com").roleId("owner-role").build();

        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("owner@test.com", "pass"));

        when(userRepository.findByEmailAndStatusNot("owner@test.com", UserStatus.DELETED))
                .thenReturn(Optional.of(currentUser));
        when(roleRepository.findById("owner-role")).thenReturn(Optional.of(ownerRole));

        Role systemRole = Role.builder()
                .id("manager-role")
                .name("MANAGER")
                .systemRole(true)
                .permissionIds(new java.util.ArrayList<>(List.of("p1")))
                .build();
        when(roleRepository.findById("manager-role")).thenReturn(Optional.of(systemRole));

        com.viraj.dmabackend.auth.dto.RemovePermissionsRequest request = new com.viraj.dmabackend.auth.dto.RemovePermissionsRequest();
        request.setPermissionIds(List.of("p1"));

        org.junit.jupiter.api.Assertions.assertThrows(
                BadRequestException.class,
                () -> roleService.removePermissions("manager-role", request)
        );
    }
}
