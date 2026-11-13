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
import org.junit.jupiter.api.AfterEach;
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

    @Mock
    private org.springframework.data.mongodb.core.MongoTemplate mongoTemplate;

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

    @AfterEach
    void tearDown() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
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

        User updatedUser = new User();
        updatedUser.setFailedLoginAttempts(1);

        when(userRepository.findByEmailAndStatusNot("test@agency.com", UserStatus.DELETED))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashedpassword")).thenReturn(false);
        when(mongoTemplate.findAndModify(any(), any(), any(), eq(User.class))).thenReturn(updatedUser);

        assertThrows(UnauthorizedException.class, () -> authService.login(request));

        assertEquals(1, user.getFailedLoginAttempts());
        assertNull(user.getLockoutUntil());
        verify(mongoTemplate).findAndModify(any(), any(), any(), eq(User.class));
        verify(userRepository, never()).save(user);
    }

    @Test
    void login_LocksAccountOnFifthFailedAttempt() {
        user.setFailedLoginAttempts(4);

        LoginRequest request = new LoginRequest();
        request.setEmail("test@agency.com");
        request.setPassword("wrong");

        User updatedUser = new User();
        updatedUser.setFailedLoginAttempts(5);

        when(userRepository.findByEmailAndStatusNot("test@agency.com", UserStatus.DELETED))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashedpassword")).thenReturn(false);
        when(mongoTemplate.findAndModify(any(), any(), any(), eq(User.class))).thenReturn(updatedUser);

        assertThrows(UnauthorizedException.class, () -> authService.login(request));

        assertEquals(5, user.getFailedLoginAttempts());
        assertNotNull(user.getLockoutUntil());
        assertTrue(user.getLockoutUntil().isAfter(LocalDateTime.now(java.time.ZoneOffset.UTC)));
        verify(mongoTemplate).findAndModify(any(), any(), any(), eq(User.class));
        verify(mongoTemplate).updateFirst(any(), any(), eq(User.class));
        verify(userRepository, never()).save(user);
    }

    @Test
    void login_BlocksLoginWhenAccountIsLocked() {
        user.setLockoutUntil(LocalDateTime.now(java.time.ZoneOffset.UTC).plusMinutes(10));

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
        verify(mongoTemplate).updateFirst(any(), any(), eq(User.class));
        verify(userRepository, never()).save(user);
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
    void logout_whenValidActiveToken_purgesAllUserRefreshTokensAndRevokesAccessTokens() {
        com.viraj.dmabackend.auth.entity.RefreshToken token = com.viraj.dmabackend.auth.entity.RefreshToken.builder()
                .id("t1")
                .token("active-token")
                .userId("user-1")
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken("active-token")).thenReturn(Optional.of(token));
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

        authService.logout("active-token");

        verify(mongoTemplate).updateFirst(any(), any(), eq(com.viraj.dmabackend.auth.entity.RefreshToken.class));
        verify(refreshTokenRepository).deleteByUserId("user-1");
        verify(userRepository).save(user);
        assertNotNull(user.getTokensRevokedAt());
    }

    @Test
    void logout_whenUserAuthenticatedInSecurityContext_purgesAllUserRefreshTokensAndRevokesAccessTokens() {
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("test@agency.com", "pass"));

        when(userRepository.findByEmailAndStatusNot("test@agency.com", UserStatus.DELETED))
                .thenReturn(Optional.of(user));

        authService.logout(null);

        verify(refreshTokenRepository).deleteByUserId("user-1");
        verify(userRepository).save(user);
        assertNotNull(user.getTokensRevokedAt());
    }

    @Test
    void logout_whenNullOrBlank_completesCleanlyWithoutRepoCall() {
        assertDoesNotThrow(() -> authService.logout(null));
        assertDoesNotThrow(() -> authService.logout("   "));

        verify(mongoTemplate, never()).updateFirst(any(), any(), any(Class.class));
    }

    @Test
    void login_ResetsCounterWhenLockoutExpired_IncrementsToOneOnWrongPassword() {
        user.setFailedLoginAttempts(5);
        user.setLockoutUntil(LocalDateTime.now(java.time.ZoneOffset.UTC).minusMinutes(5));

        LoginRequest request = new LoginRequest();
        request.setEmail("test@agency.com");
        request.setPassword("wrong");

        User updatedUser = new User();
        updatedUser.setFailedLoginAttempts(1);

        when(userRepository.findByEmailAndStatusNot("test@agency.com", UserStatus.DELETED))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashedpassword")).thenReturn(false);
        when(mongoTemplate.findAndModify(any(), any(), any(), eq(User.class))).thenReturn(updatedUser);

        assertThrows(UnauthorizedException.class, () -> authService.login(request));

        assertEquals(1, user.getFailedLoginAttempts());
        assertNull(user.getLockoutUntil());
        verify(mongoTemplate, atLeastOnce()).updateFirst(any(), any(), eq(User.class));
        verify(mongoTemplate).findAndModify(any(), any(), any(), eq(User.class));
        verify(userRepository, never()).save(user);
    }

    @Test
    void login_ResetsAndPersistsCounterWhenLockoutExpired_OnSuccessfulLogin() {
        user.setFailedLoginAttempts(5);
        user.setLockoutUntil(LocalDateTime.now(java.time.ZoneOffset.UTC).minusMinutes(5));

        LoginRequest request = new LoginRequest();
        request.setEmail("test@agency.com");
        request.setPassword("correctPassword");

        com.viraj.dmabackend.auth.entity.Role role = new com.viraj.dmabackend.auth.entity.Role();
        role.setId("role-1");
        role.setName("EMPLOYEE");

        when(userRepository.findByEmailAndStatusNot("test@agency.com", UserStatus.DELETED))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correctPassword", "hashedpassword")).thenReturn(true);
        when(roleRepository.findById("role-1")).thenReturn(Optional.of(role));
        when(permissionRepository.findAllById(any())).thenReturn(java.util.Collections.emptyList());
        when(jwtUtil.generateToken(any(), any(), any(), anyBoolean())).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken()).thenReturn("refresh-token");
        when(userMapper.toUserResponse(any(), any())).thenReturn(com.viraj.dmabackend.auth.dto.UserResponse.builder().build());

        authService.login(request);

        assertEquals(0, user.getFailedLoginAttempts());
        assertNull(user.getLockoutUntil());
        verify(mongoTemplate).updateFirst(any(), any(), eq(User.class));
        verify(userRepository, never()).save(user);
    }

    @Test
    void changePassword_ThrowsWhenNewPasswordHasWhitespace() {
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("test@agency.com", "pass"));

        when(userRepository.findByEmailAndStatusNot("test@agency.com", UserStatus.DELETED))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("currentPass123", "hashedpassword")).thenReturn(true);

        com.viraj.dmabackend.auth.dto.ChangePasswordRequest request = new com.viraj.dmabackend.auth.dto.ChangePasswordRequest();
        request.setCurrentPassword("currentPass123");
        request.setNewPassword("  newSecretPass  ");

        com.viraj.dmabackend.exception.BadRequestException ex =
                assertThrows(com.viraj.dmabackend.exception.BadRequestException.class, () -> authService.changePassword(request));
        assertTrue(ex.getMessage().contains("whitespace"));
    }

    @Test
    void changePassword_ThrowsWhenUserIsNotActive() {
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("test@agency.com", "pass"));

        user.setStatus(UserStatus.INACTIVE);
        when(userRepository.findByEmailAndStatusNot("test@agency.com", UserStatus.DELETED))
                .thenReturn(Optional.of(user));

        com.viraj.dmabackend.auth.dto.ChangePasswordRequest request = new com.viraj.dmabackend.auth.dto.ChangePasswordRequest();
        request.setCurrentPassword("currentPass123");
        request.setNewPassword("newSecretPass123");

        UnauthorizedException ex =
                assertThrows(UnauthorizedException.class, () -> authService.changePassword(request));
        assertEquals("User account is not active", ex.getMessage());
    }

    @Test
    void refresh_withValidToken_rotatesAndReturnsNewAuthenticationResponse() {
        com.viraj.dmabackend.auth.entity.RefreshToken activeToken = com.viraj.dmabackend.auth.entity.RefreshToken.builder()
                .id("t1")
                .token("valid-token")
                .userId("user-1")
                .expiresAt(LocalDateTime.now(java.time.ZoneOffset.UTC).plusDays(1))
                .revoked(false)
                .build();

        com.viraj.dmabackend.auth.entity.Role role = new com.viraj.dmabackend.auth.entity.Role();
        role.setId("role-1");
        role.setName("EMPLOYEE");

        when(mongoTemplate.findAndModify(any(), any(), any(), eq(com.viraj.dmabackend.auth.entity.RefreshToken.class)))
                .thenReturn(activeToken);
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(roleRepository.findById("role-1")).thenReturn(Optional.of(role));
        when(permissionRepository.findAllById(any())).thenReturn(java.util.Collections.emptyList());
        when(jwtUtil.generateRefreshToken()).thenReturn("new-rotated-token");
        when(jwtUtil.generateToken(any(), any(), any(), anyBoolean())).thenReturn("new-access-token");
        when(userMapper.toUserResponse(any(), any())).thenReturn(com.viraj.dmabackend.auth.dto.UserResponse.builder().build());

        com.viraj.dmabackend.auth.dto.AuthenticationResponse response = authService.refresh("valid-token");

        assertNotNull(response);
        assertEquals("new-access-token", response.getToken());
        verify(refreshTokenRepository).save(any(com.viraj.dmabackend.auth.entity.RefreshToken.class));
    }

    @Test
    void refresh_withExpiredToken_throwsUnauthorizedException() {
        com.viraj.dmabackend.auth.entity.RefreshToken expiredToken = com.viraj.dmabackend.auth.entity.RefreshToken.builder()
                .id("t1")
                .token("expired-token")
                .userId("user-1")
                .expiresAt(LocalDateTime.now(java.time.ZoneOffset.UTC).minusDays(1))
                .revoked(false)
                .build();

        when(mongoTemplate.findAndModify(any(), any(), any(), eq(com.viraj.dmabackend.auth.entity.RefreshToken.class)))
                .thenReturn(expiredToken);

        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> authService.refresh("expired-token"));
        assertEquals("Refresh token has expired", ex.getMessage());
    }

    @Test
    void refresh_withInactiveUser_throwsUnauthorizedException() {
        com.viraj.dmabackend.auth.entity.RefreshToken activeToken = com.viraj.dmabackend.auth.entity.RefreshToken.builder()
                .id("t1")
                .token("valid-token")
                .userId("user-1")
                .expiresAt(LocalDateTime.now(java.time.ZoneOffset.UTC).plusDays(1))
                .revoked(false)
                .build();

        user.setStatus(UserStatus.INACTIVE);

        when(mongoTemplate.findAndModify(any(), any(), any(), eq(com.viraj.dmabackend.auth.entity.RefreshToken.class)))
                .thenReturn(activeToken);
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> authService.refresh("valid-token"));
        assertEquals("User account is not active", ex.getMessage());
    }

    @Test
    void refresh_withCompromisedReusedToken_deletesAllUserTokensAndThrowsUnauthorizedException() {
        when(mongoTemplate.findAndModify(any(), any(), any(), eq(com.viraj.dmabackend.auth.entity.RefreshToken.class)))
                .thenReturn(null);

        com.viraj.dmabackend.auth.entity.RefreshToken stolenToken = com.viraj.dmabackend.auth.entity.RefreshToken.builder()
                .id("t1")
                .token("stolen-token")
                .userId("user-1")
                .revoked(true)
                .revokedAt(LocalDateTime.now(java.time.ZoneOffset.UTC).minusMinutes(5))
                .build();

        when(refreshTokenRepository.findByToken("stolen-token")).thenReturn(Optional.of(stolenToken));
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> authService.refresh("stolen-token"));
        assertEquals("Refresh token was already revoked", ex.getMessage());
        verify(refreshTokenRepository).deleteByUserId("user-1");
        verify(userRepository).save(user);
        assertNotNull(user.getTokensRevokedAt());
    }

    @Test
    void changePassword_RevokesPreviousTokensAndMintsNewTokenWithAdjustedIssuedAt() {
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("test@agency.com", "pass"));

        Role role = new Role();
        role.setId("role-1");
        role.setName("EMPLOYEE");

        when(userRepository.findByEmailAndStatusNot("test@agency.com", UserStatus.DELETED))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("currentPass123", "hashedpassword")).thenReturn(true);
        when(passwordEncoder.matches("newSecretPass123", "hashedpassword")).thenReturn(false);
        when(passwordEncoder.encode("newSecretPass123")).thenReturn("newHashedPassword");
        when(roleRepository.findById("role-1")).thenReturn(Optional.of(role));
        when(permissionRepository.findAllById(any())).thenReturn(java.util.Collections.emptyList());
        when(jwtUtil.generateRefreshToken()).thenReturn("new-refresh-token");
        when(jwtUtil.generateToken(any(), any(), any(), anyBoolean(), any(java.util.Date.class))).thenReturn("replacement-jwt-token");
        when(userMapper.toUserResponse(any(), any())).thenReturn(com.viraj.dmabackend.auth.dto.UserResponse.builder().build());

        com.viraj.dmabackend.auth.dto.ChangePasswordRequest request = new com.viraj.dmabackend.auth.dto.ChangePasswordRequest();
        request.setCurrentPassword("currentPass123");
        request.setNewPassword("newSecretPass123");

        com.viraj.dmabackend.auth.dto.AuthenticationResponse response = authService.changePassword(request);

        assertNotNull(response);
        assertEquals("replacement-jwt-token", response.getToken());
        verify(refreshTokenRepository).deleteByUserId("user-1");
        verify(userRepository).save(user);
        assertNotNull(user.getTokensRevokedAt());
    }

    @Test
    void refresh_withRecentlyRotatedTokenWithinGraceWindow_doesNotDeleteUserTokens() {
        when(mongoTemplate.findAndModify(any(), any(), any(), eq(com.viraj.dmabackend.auth.entity.RefreshToken.class)))
                .thenReturn(null);

        com.viraj.dmabackend.auth.entity.RefreshToken recentlyRotated = com.viraj.dmabackend.auth.entity.RefreshToken.builder()
                .id("t1")
                .token("concurrent-token")
                .userId("user-1")
                .revoked(true)
                .revokedAt(LocalDateTime.now(java.time.ZoneOffset.UTC).minusSeconds(2))
                .build();

        when(refreshTokenRepository.findByToken("concurrent-token")).thenReturn(Optional.of(recentlyRotated));

        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> authService.refresh("concurrent-token"));
        assertTrue(ex.getMessage().contains("recently rotated"));
        verify(refreshTokenRepository, never()).deleteByUserId(any());
    }

    @Test
    void refresh_withNullOrBlankToken_throwsUnauthorizedException() {
        assertThrows(UnauthorizedException.class, () -> authService.refresh(null));
        assertThrows(UnauthorizedException.class, () -> authService.refresh("   "));
        verify(mongoTemplate, never()).findAndModify(any(), any(), any(), any(Class.class));
    }
}
