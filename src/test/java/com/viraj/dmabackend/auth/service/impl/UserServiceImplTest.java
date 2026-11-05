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

import static org.junit.jupiter.api.Assertions.assertThrows;
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
}
