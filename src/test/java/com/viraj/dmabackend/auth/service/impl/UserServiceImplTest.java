package com.viraj.dmabackend.auth.service.impl;

import com.viraj.dmabackend.auth.dto.UpdateUserRequest;
import com.viraj.dmabackend.auth.dto.UpdateUserStatusRequest;
import com.viraj.dmabackend.auth.entity.Role;
import com.viraj.dmabackend.auth.entity.User;
import com.viraj.dmabackend.auth.enums.UserStatus;
import com.viraj.dmabackend.auth.exception.UnauthorizedRoleAssignmentException;
import com.viraj.dmabackend.auth.mapper.UserMapper;
import com.viraj.dmabackend.auth.repository.RefreshTokenRepository;
import com.viraj.dmabackend.auth.repository.RoleRepository;
import com.viraj.dmabackend.auth.repository.UserRepository;
import com.viraj.dmabackend.auth.validator.UserValidator;
import com.viraj.dmabackend.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserValidator userValidator;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User ownerUser;
    private Role ownerRole;
    private User managerUser;
    private Role managerRole;

    @BeforeEach
    void setUp() {
        ownerRole = new Role();
        ownerRole.setId("role-owner");
        ownerRole.setName("OWNER");

        managerRole = new Role();
        managerRole.setId("role-manager");
        managerRole.setName("MANAGER");

        ownerUser = new User();
        ownerUser.setId("user-owner");
        ownerUser.setEmail("owner@agency.com");
        ownerUser.setRoleId("role-owner");
        ownerUser.setStatus(UserStatus.ACTIVE);

        managerUser = new User();
        managerUser.setId("user-manager");
        managerUser.setEmail("manager@agency.com");
        managerUser.setRoleId("role-manager");
        managerUser.setStatus(UserStatus.ACTIVE);
    }

    private void authenticateAs(User user) {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user.getEmail(), null, java.util.Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
        lenient().when(userRepository.findByEmailAndStatusNot(user.getEmail(), UserStatus.DELETED))
                .thenReturn(Optional.of(user));
    }

    @Test
    void softDeleteUser_ThrowsWhenDeletingOwner() {
        authenticateAs(managerUser);
        when(userRepository.findByIdAndStatusNot("user-owner", UserStatus.DELETED))
                .thenReturn(Optional.of(ownerUser));
        when(roleRepository.findById("role-owner")).thenReturn(Optional.of(ownerRole));

        assertThrows(BadRequestException.class, () -> userService.softDeleteUser("user-owner"));
    }

    @Test
    void softDeleteUser_ThrowsWhenSelfDeleting() {
        authenticateAs(managerUser);
        when(userRepository.findByIdAndStatusNot("user-manager", UserStatus.DELETED))
                .thenReturn(Optional.of(managerUser));
        when(roleRepository.findById("role-manager")).thenReturn(Optional.of(managerRole));

        assertThrows(BadRequestException.class, () -> userService.softDeleteUser("user-manager"));
    }

    @Test
    void updateUserStatus_ThrowsWhenModifyingOwnerStatus() {
        authenticateAs(managerUser);
        when(userRepository.findByIdAndStatusNot("user-owner", UserStatus.DELETED))
                .thenReturn(Optional.of(ownerUser));
        when(roleRepository.findById("role-owner")).thenReturn(Optional.of(ownerRole));

        UpdateUserStatusRequest request = new UpdateUserStatusRequest();
        request.setStatus(UserStatus.SUSPENDED);

        assertThrows(BadRequestException.class, () -> userService.updateUserStatus("user-owner", request));
    }

    @Test
    void updateUserStatus_ThrowsWhenSelfDeactivating() {
        authenticateAs(managerUser);
        when(userRepository.findByIdAndStatusNot("user-manager", UserStatus.DELETED))
                .thenReturn(Optional.of(managerUser));
        when(roleRepository.findById("role-manager")).thenReturn(Optional.of(managerRole));

        UpdateUserStatusRequest request = new UpdateUserStatusRequest();
        request.setStatus(UserStatus.SUSPENDED);

        assertThrows(BadRequestException.class, () -> userService.updateUserStatus("user-manager", request));
    }

    @Test
    void updateUser_ThrowsWhenNonOwnerModifiesOwner() {
        authenticateAs(managerUser);
        when(userRepository.findByIdAndStatusNot("user-owner", UserStatus.DELETED))
                .thenReturn(Optional.of(ownerUser));
        when(roleRepository.findById("role-owner")).thenReturn(Optional.of(ownerRole));
        when(roleRepository.findById("role-manager")).thenReturn(Optional.of(managerRole));

        UpdateUserRequest request = new UpdateUserRequest();
        request.setRoleId("role-manager");
        request.setPhoneNumber("+1234567890");

        assertThrows(UnauthorizedRoleAssignmentException.class,
                () -> userService.updateUser("user-owner", request));
    }

    @Test
    void filterUsersByStatus_ThrowsWhenStatusIsDeleted() {
        assertThrows(BadRequestException.class,
                () -> userService.filterUsersByStatus(UserStatus.DELETED, org.springframework.data.domain.Pageable.unpaged()));
    }

    @Test
    void updateUserStatus_deactivatesUserAndDeletesRefreshTokens() {
        authenticateAs(ownerUser);
        when(userRepository.findByIdAndStatusNot("user-manager", UserStatus.DELETED))
                .thenReturn(Optional.of(managerUser));
        when(roleRepository.findById("role-manager")).thenReturn(Optional.of(managerRole));
        when(roleRepository.findById("role-owner")).thenReturn(Optional.of(ownerRole));
        when(userRepository.findByEmailAndStatusNot("owner@agency.com", UserStatus.DELETED))
                .thenReturn(Optional.of(ownerUser));
        when(userRepository.save(any(User.class))).thenReturn(managerUser);
        when(userMapper.toUserResponse(any(User.class), any())).thenReturn(com.viraj.dmabackend.auth.dto.UserResponse.builder().build());

        UpdateUserStatusRequest request = new UpdateUserStatusRequest();
        request.setStatus(UserStatus.SUSPENDED);

        userService.updateUserStatus("user-manager", request);

        verify(refreshTokenRepository).deleteByUserId("user-manager");
        verify(userRepository).save(managerUser);
    }

    @Test
    void updateUserStatus_activatesUserAndClearsLockout() {
        authenticateAs(ownerUser);
        managerUser.setStatus(UserStatus.LOCKED);
        managerUser.setFailedLoginAttempts(5);
        managerUser.setLockoutUntil(java.time.LocalDateTime.now().plusMinutes(15));

        when(userRepository.findByIdAndStatusNot("user-manager", UserStatus.DELETED))
                .thenReturn(Optional.of(managerUser));
        when(roleRepository.findById("role-manager")).thenReturn(Optional.of(managerRole));
        when(roleRepository.findById("role-owner")).thenReturn(Optional.of(ownerRole));
        when(userRepository.findByEmailAndStatusNot("owner@agency.com", UserStatus.DELETED))
                .thenReturn(Optional.of(ownerUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(userMapper.toUserResponse(any(User.class), any())).thenReturn(com.viraj.dmabackend.auth.dto.UserResponse.builder().build());

        UpdateUserStatusRequest request = new UpdateUserStatusRequest();
        request.setStatus(UserStatus.ACTIVE);

        userService.updateUserStatus("user-manager", request);

        org.junit.jupiter.api.Assertions.assertEquals(UserStatus.ACTIVE, managerUser.getStatus());
        org.junit.jupiter.api.Assertions.assertEquals(0, managerUser.getFailedLoginAttempts());
        org.junit.jupiter.api.Assertions.assertNull(managerUser.getLockoutUntil());
        verify(userRepository).save(managerUser);
    }

    @Test
    void softDeleteUser_deletesRefreshTokens() {
        authenticateAs(ownerUser);
        when(userRepository.findByIdAndStatusNot("user-manager", UserStatus.DELETED))
                .thenReturn(Optional.of(managerUser));
        when(roleRepository.findById("role-manager")).thenReturn(Optional.of(managerRole));
        when(roleRepository.findById("role-owner")).thenReturn(Optional.of(ownerRole));
        when(userRepository.findByEmailAndStatusNot("owner@agency.com", UserStatus.DELETED))
                .thenReturn(Optional.of(ownerUser));
        when(userRepository.save(any(User.class))).thenReturn(managerUser);
        when(userMapper.toUserResponse(any(User.class), any())).thenReturn(com.viraj.dmabackend.auth.dto.UserResponse.builder().build());

        userService.softDeleteUser("user-manager");

        verify(refreshTokenRepository).deleteByUserId("user-manager");
        verify(userRepository).save(managerUser);
    }

    @Test
    void updateUser_deletesRefreshTokensWhenRoleChanges() {
        authenticateAs(ownerUser);
        Role employeeRole = new Role();
        employeeRole.setId("role-employee");
        employeeRole.setName("EMPLOYEE");

        when(userRepository.findByIdAndStatusNot("user-manager", UserStatus.DELETED))
                .thenReturn(Optional.of(managerUser));
        when(roleRepository.findById("role-manager")).thenReturn(Optional.of(managerRole));
        when(roleRepository.findById("role-employee")).thenReturn(Optional.of(employeeRole));
        when(roleRepository.findById("role-owner")).thenReturn(Optional.of(ownerRole));
        when(userRepository.findByEmailAndStatusNot("owner@agency.com", UserStatus.DELETED))
                .thenReturn(Optional.of(ownerUser));
        when(userRepository.save(any(User.class))).thenReturn(managerUser);
        when(userMapper.toUserResponse(any(User.class), any())).thenReturn(com.viraj.dmabackend.auth.dto.UserResponse.builder().build());

        com.viraj.dmabackend.auth.dto.UpdateUserRequest request = new com.viraj.dmabackend.auth.dto.UpdateUserRequest();
        request.setFirstName("Manager");
        request.setLastName("User");
        request.setPhoneNumber("9876543210");
        request.setRoleId("role-employee");

        userService.updateUser("user-manager", request);

        verify(refreshTokenRepository).deleteByUserId("user-manager");
        verify(userRepository).save(managerUser);
    }

    @Test
    void updateUser_doesNotDeleteRefreshTokensWhenRoleUnchanged() {
        authenticateAs(ownerUser);

        when(userRepository.findByIdAndStatusNot("user-manager", UserStatus.DELETED))
                .thenReturn(Optional.of(managerUser));
        when(roleRepository.findById("role-manager")).thenReturn(Optional.of(managerRole));
        when(roleRepository.findById("role-owner")).thenReturn(Optional.of(ownerRole));
        when(userRepository.findByEmailAndStatusNot("owner@agency.com", UserStatus.DELETED))
                .thenReturn(Optional.of(ownerUser));
        when(userRepository.save(any(User.class))).thenReturn(managerUser);
        when(userMapper.toUserResponse(any(User.class), any())).thenReturn(com.viraj.dmabackend.auth.dto.UserResponse.builder().build());

        com.viraj.dmabackend.auth.dto.UpdateUserRequest request = new com.viraj.dmabackend.auth.dto.UpdateUserRequest();
        request.setFirstName("NewManager");
        request.setLastName("User");
        request.setPhoneNumber("9876543210");
        request.setRoleId("role-manager");

        userService.updateUser("user-manager", request);

        verify(refreshTokenRepository, never()).deleteByUserId("user-manager");
        verify(userRepository).save(managerUser);
    }

    @Test
    void getAllUsers_WhenRoleMissingOrNull_GracefullyFallsBackToUnknownRole() {
        User user1 = User.builder().id("u1").roleId("missing-role").build();
        User user2 = User.builder().id("u2").roleId(null).build();
        org.springframework.data.domain.Page<User> page =
                new org.springframework.data.domain.PageImpl<>(java.util.List.of(user1, user2));

        when(userRepository.findByStatusNot(eq(UserStatus.DELETED), any())).thenReturn(page);
        when(roleRepository.findAllById(java.util.List.of("missing-role"))).thenReturn(java.util.Collections.emptyList());
        when(userMapper.toUserResponse(any(User.class), eq("UNKNOWN")))
                .thenReturn(com.viraj.dmabackend.auth.dto.UserResponse.builder().roleName("UNKNOWN").build());

        org.springframework.data.domain.Page<com.viraj.dmabackend.auth.dto.UserResponse> result =
                userService.getAllUsers(org.springframework.data.domain.PageRequest.of(0, 10));

        org.junit.jupiter.api.Assertions.assertNotNull(result);
        org.junit.jupiter.api.Assertions.assertEquals(2, result.getContent().size());
        verify(userMapper, times(2)).toUserResponse(any(User.class), eq("UNKNOWN"));
    }

    @Test
    void updateUser_ManagerCanUpdateSelf_RetainingRole() {
        authenticateAs(managerUser);

        when(userRepository.findByIdAndStatusNot("user-manager", UserStatus.DELETED))
                .thenReturn(Optional.of(managerUser));
        when(roleRepository.findById("role-manager")).thenReturn(Optional.of(managerRole));
        when(userRepository.findByEmailAndStatusNot("manager@agency.com", UserStatus.DELETED))
                .thenReturn(Optional.of(managerUser));
        when(userRepository.save(any(User.class))).thenReturn(managerUser);
        when(userMapper.toUserResponse(any(User.class), any())).thenReturn(com.viraj.dmabackend.auth.dto.UserResponse.builder().build());

        com.viraj.dmabackend.auth.dto.UpdateUserRequest request = new com.viraj.dmabackend.auth.dto.UpdateUserRequest();
        request.setFirstName("UpdatedManager");
        request.setLastName("User");
        request.setPhoneNumber("9876543210");
        request.setRoleId("role-manager");

        userService.updateUser("user-manager", request);

        verify(userValidator).validateUserManagement(managerUser, managerRole, managerUser, managerRole);
        verify(userValidator, never()).validateRoleAssignment(any(), any(), any());
        verify(userRepository).save(managerUser);
    }

    @Test
    void updateUser_ManagerCannotChangeOwnRole() {
        authenticateAs(managerUser);

        Role employeeRole = new Role();
        employeeRole.setId("role-employee");
        employeeRole.setName("EMPLOYEE");

        when(userRepository.findByIdAndStatusNot("user-manager", UserStatus.DELETED))
                .thenReturn(Optional.of(managerUser));
        when(roleRepository.findById("role-manager")).thenReturn(Optional.of(managerRole));
        when(roleRepository.findById("role-employee")).thenReturn(Optional.of(employeeRole));
        when(userRepository.findByEmailAndStatusNot("manager@agency.com", UserStatus.DELETED))
                .thenReturn(Optional.of(managerUser));

        com.viraj.dmabackend.auth.dto.UpdateUserRequest request = new com.viraj.dmabackend.auth.dto.UpdateUserRequest();
        request.setFirstName("UpdatedManager");
        request.setLastName("User");
        request.setPhoneNumber("9876543210");
        request.setRoleId("role-employee");

        assertThrows(BadRequestException.class, () -> userService.updateUser("user-manager", request));
    }

    @Test
    void createUser_SetsPasswordChangeRequiredToTrue() {
        authenticateAs(ownerUser);

        Role employeeRole = new Role();
        employeeRole.setId("role-employee");
        employeeRole.setName("EMPLOYEE");

        when(roleRepository.findById("role-employee")).thenReturn(Optional.of(employeeRole));
        when(roleRepository.findById("role-owner")).thenReturn(Optional.of(ownerRole));
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-pwd");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        com.viraj.dmabackend.auth.dto.CreateUserRequest request = new com.viraj.dmabackend.auth.dto.CreateUserRequest();
        request.setFirstName("New");
        request.setLastName("Employee");
        request.setEmail("new.employee@agency.com");
        request.setPhoneNumber("9123456780");
        request.setRoleId("role-employee");

        userService.createUser(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User savedUser = captor.getValue();
        assertTrue(savedUser.isPasswordChangeRequired(), "Newly created user must have passwordChangeRequired set to true");
        assertEquals("new.employee@agency.com", savedUser.getEmail());
    }

    @Test
    void getUserById_TrimsUserId() {
        when(userRepository.findByIdAndStatusNot("user-1", UserStatus.DELETED))
                .thenReturn(Optional.of(managerUser));
        when(roleRepository.findById(managerUser.getRoleId())).thenReturn(Optional.of(managerRole));
        when(userMapper.toUserResponse(managerUser, managerRole.getName()))
                .thenReturn(com.viraj.dmabackend.auth.dto.UserResponse.builder().id("user-1").build());

        com.viraj.dmabackend.auth.dto.UserResponse response = userService.getUserById("   user-1   ");

        org.junit.jupiter.api.Assertions.assertNotNull(response);
        verify(userRepository).findByIdAndStatusNot("user-1", UserStatus.DELETED);
    }

    @Test
    void getAllUsers_ClampsOversizedPageable() {
        org.springframework.data.domain.PageRequest oversized = org.springframework.data.domain.PageRequest.of(0, 500);
        when(userRepository.findByStatusNot(eq(UserStatus.DELETED), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(org.springframework.data.domain.Page.empty());

        userService.getAllUsers(oversized);

        ArgumentCaptor<org.springframework.data.domain.Pageable> captor = ArgumentCaptor.forClass(org.springframework.data.domain.Pageable.class);
        verify(userRepository).findByStatusNot(eq(UserStatus.DELETED), captor.capture());
        assertEquals(100, captor.getValue().getPageSize());
    }
}
