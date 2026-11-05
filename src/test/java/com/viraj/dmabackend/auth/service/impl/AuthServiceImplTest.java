package com.viraj.dmabackend.auth.service.impl;

import com.viraj.dmabackend.auth.dto.LoginRequest;
import com.viraj.dmabackend.auth.entity.Role;
import com.viraj.dmabackend.auth.entity.User;
import com.viraj.dmabackend.auth.enums.UserStatus;
import com.viraj.dmabackend.auth.repository.PermissionRepository;
import com.viraj.dmabackend.auth.repository.RefreshTokenRepository;
import com.viraj.dmabackend.auth.repository.RoleRepository;
import com.viraj.dmabackend.auth.repository.UserRepository;
import com.viraj.dmabackend.auth.security.JwtUtil;
import com.viraj.dmabackend.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private com.viraj.dmabackend.auth.mapper.UserMapper userMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "refreshExpiration", 604800000L);

        user = new User();
        user.setId("user-1");
        user.setEmail("test@agency.com");
        user.setPassword("hashedpassword");
        user.setStatus(UserStatus.ACTIVE);
        user.setFailedLoginAttempts(0);
        user.setRoleId("role-1");
    }

    @Test
    void login_NormalizesEmailCaseAndWhitespace() {
        LoginRequest request = new LoginRequest();
        request.setEmail("  Test@Agency.COM  ");
        request.setPassword("correct_password");

        Role role = new Role();
        role.setId("role-1");
        role.setName("EMPLOYEE");
        role.setPermissionIds(null);

        when(userRepository.findByEmailAndStatusNot("test@agency.com", UserStatus.DELETED))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correct_password", "hashedpassword")).thenReturn(true);
        when(roleRepository.findById("role-1")).thenReturn(Optional.of(role));
        when(permissionRepository.findAllById(any())).thenReturn(java.util.Collections.emptyList());
        when(jwtUtil.generateRefreshToken()).thenReturn("new-refresh-token");
        when(jwtUtil.generateToken(any(), any(), any(), anyBoolean())).thenReturn("jwt-token");
        when(userMapper.toUserResponse(any(), any())).thenReturn(com.viraj.dmabackend.auth.dto.UserResponse.builder().build());

        com.viraj.dmabackend.auth.dto.AuthenticationResponse response = authService.login(request);

        assertNotNull(response);
        verify(userRepository).findByEmailAndStatusNot("test@agency.com", UserStatus.DELETED);
    }

    @Test
    void login_IncrementsFailedAttemptsOnBadPassword() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@agency.com");
        request.setPassword("wrong");

        when(userRepository.findByEmailAndStatusNot("test@agency.com", UserStatus.DELETED))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashedpassword")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(request));

        assertEquals(1, user.getFailedLoginAttempts());
        assertNull(user.getLockoutUntil());
        verify(userRepository).save(user);
    }

    @Test
    void login_LocksAccountOnFifthFailedAttempt() {
        user.setFailedLoginAttempts(4);

        LoginRequest request = new LoginRequest();
        request.setEmail("test@agency.com");
        request.setPassword("wrong");

        when(userRepository.findByEmailAndStatusNot("test@agency.com", UserStatus.DELETED))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashedpassword")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(request));

        assertEquals(5, user.getFailedLoginAttempts());
        assertNotNull(user.getLockoutUntil());
        assertTrue(user.getLockoutUntil().isAfter(LocalDateTime.now()));
        verify(userRepository).save(user);
    }

    @Test
    void login_BlocksLoginWhenAccountIsLocked() {
        user.setLockoutUntil(LocalDateTime.now().plusMinutes(10));

        LoginRequest request = new LoginRequest();
        request.setEmail("test@agency.com");
        request.setPassword("correct_password");

        when(userRepository.findByEmailAndStatusNot("test@agency.com", UserStatus.DELETED))
                .thenReturn(Optional.of(user));

        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> authService.login(request));
        assertTrue(ex.getMessage().contains("temporarily locked"));
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void login_ResetsFailedAttemptsOnSuccessfulLogin() {
        user.setFailedLoginAttempts(3);

        LoginRequest request = new LoginRequest();
        request.setEmail("test@agency.com");
        request.setPassword("correct_password");

        Role role = new Role();
        role.setId("role-1");
        role.setName("EMPLOYEE");
        role.setPermissionIds(java.util.Collections.emptyList());

        when(userRepository.findByEmailAndStatusNot("test@agency.com", UserStatus.DELETED))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correct_password", "hashedpassword")).thenReturn(true);
        when(roleRepository.findById("role-1")).thenReturn(Optional.of(role));
        when(permissionRepository.findAllById(any())).thenReturn(java.util.Collections.emptyList());
        when(jwtUtil.generateRefreshToken()).thenReturn("new-refresh-token");
        when(jwtUtil.generateToken(any(), any(), any(), anyBoolean())).thenReturn("jwt-token");

        authService.login(request);

        assertEquals(0, user.getFailedLoginAttempts());
        assertNull(user.getLockoutUntil());
        verify(userRepository).save(user);
    }

    @Test
    void login_whenRolePermissionIdsIsNull_generatesAuthenticationResponseSuccessfully() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@agency.com");
        request.setPassword("correct_password");

        Role role = new Role();
        role.setId("role-1");
        role.setName("EMPLOYEE");
        role.setPermissionIds(null);

        when(userRepository.findByEmailAndStatusNot("test@agency.com", UserStatus.DELETED))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correct_password", "hashedpassword")).thenReturn(true);
        when(roleRepository.findById("role-1")).thenReturn(Optional.of(role));
        when(permissionRepository.findAllById(java.util.Collections.emptyList())).thenReturn(java.util.Collections.emptyList());
        when(jwtUtil.generateRefreshToken()).thenReturn("new-refresh-token");
        when(jwtUtil.generateToken(any(), any(), any(), anyBoolean())).thenReturn("jwt-token");
        when(userMapper.toUserResponse(any(), any())).thenReturn(com.viraj.dmabackend.auth.dto.UserResponse.builder().build());

        com.viraj.dmabackend.auth.dto.AuthenticationResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        verify(permissionRepository).findAllById(java.util.Collections.emptyList());
    }

    @Test
    void logout_whenValidActiveToken_revokesAndSaves() {
        com.viraj.dmabackend.auth.entity.RefreshToken token = com.viraj.dmabackend.auth.entity.RefreshToken.builder()
                .id("token-1")
                .token("active-token")
                .revoked(false)
                .build();
        when(refreshTokenRepository.findByToken("active-token")).thenReturn(Optional.of(token));

        authService.logout("active-token");

        assertTrue(token.isRevoked());
        verify(refreshTokenRepository).save(token);
    }

    @Test
    void logout_whenAlreadyRevokedToken_doesNotReSaveAndCompletesCleanly() {
        com.viraj.dmabackend.auth.entity.RefreshToken token = com.viraj.dmabackend.auth.entity.RefreshToken.builder()
                .id("token-1")
                .token("revoked-token")
                .revoked(true)
                .build();
        when(refreshTokenRepository.findByToken("revoked-token")).thenReturn(Optional.of(token));

        assertDoesNotThrow(() -> authService.logout("revoked-token"));

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void logout_whenTokenNotFound_completesCleanlyWithoutException() {
        when(refreshTokenRepository.findByToken("non-existent")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> authService.logout("non-existent"));

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void logout_whenNullOrBlank_completesCleanlyWithoutRepoCall() {
        assertDoesNotThrow(() -> authService.logout(null));
        assertDoesNotThrow(() -> authService.logout("   "));

        verify(refreshTokenRepository, never()).findByToken(any());
    }
}
