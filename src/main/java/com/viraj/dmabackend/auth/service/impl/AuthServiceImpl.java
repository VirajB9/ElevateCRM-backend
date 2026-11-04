package com.viraj.dmabackend.auth.service.impl;

import com.viraj.dmabackend.auth.dto.AuthenticationResponse;
import com.viraj.dmabackend.auth.dto.ChangePasswordRequest;
import com.viraj.dmabackend.auth.dto.LoginRequest;
import com.viraj.dmabackend.auth.dto.UserResponse;
import com.viraj.dmabackend.auth.entity.Permission;
import com.viraj.dmabackend.auth.entity.RefreshToken;
import com.viraj.dmabackend.auth.entity.Role;
import com.viraj.dmabackend.auth.entity.User;
import com.viraj.dmabackend.auth.enums.UserStatus;
import com.viraj.dmabackend.auth.exception.RoleNotFoundException;
import com.viraj.dmabackend.auth.mapper.UserMapper;
import com.viraj.dmabackend.auth.repository.PermissionRepository;
import com.viraj.dmabackend.auth.repository.RefreshTokenRepository;
import com.viraj.dmabackend.auth.repository.RoleRepository;
import com.viraj.dmabackend.auth.repository.UserRepository;
import com.viraj.dmabackend.auth.security.JwtUtil;
import com.viraj.dmabackend.auth.service.AuthService;
import com.viraj.dmabackend.exception.BadRequestException;
import com.viraj.dmabackend.exception.UnauthorizedException;
import org.springframework.security.core.context.SecurityContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final PermissionRepository permissionRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_MINUTES = 15;

    @Value("${app.jwt.refresh-expiration}")
    private long refreshExpiration;

    @Override
    public AuthenticationResponse login(LoginRequest request) {

        User user = userRepository
                .findByEmailAndStatusNot(request.getEmail(), UserStatus.DELETED)
                .orElseThrow(() ->
                        new UnauthorizedException("Invalid email or password"));

        if (user.getLockoutUntil() != null && user.getLockoutUntil().isAfter(LocalDateTime.now())) {
            throw new UnauthorizedException("Account is temporarily locked due to multiple failed login attempts. Please try again later.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                user.setLockoutUntil(LocalDateTime.now().plusMinutes(LOCKOUT_MINUTES));
            }
            userRepository.save(user);
            throw new UnauthorizedException("Invalid email or password");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("User account is not active");
        }

        if (user.getFailedLoginAttempts() > 0 || user.getLockoutUntil() != null) {
            user.setFailedLoginAttempts(0);
            user.setLockoutUntil(null);
            userRepository.save(user);
        }

        refreshTokenRepository.deleteByUserId(user.getId());

        String refreshTokenValue = jwtUtil.generateRefreshToken();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .userId(user.getId())
                .expiresAt(
                        LocalDateTime.now()
                                .plus(java.time.Duration.ofMillis(refreshExpiration))
                )
                .build();

        refreshTokenRepository.save(refreshToken);

        return buildAuthenticationResponse(user, refreshTokenValue);
    }

    @Override
    public AuthenticationResponse refresh(String refreshTokenValue) {

        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            // Compromised token reuse detected: revoke all active tokens for this user
            refreshTokenRepository.deleteByUserId(refreshToken.getUserId());
            throw new UnauthorizedException("Refresh token was already revoked");
        }

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("Refresh token has expired");
        }

        User user = userRepository
                .findById(refreshToken.getUserId())
                .orElseThrow(() ->
                        new UnauthorizedException("User not found"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
            throw new UnauthorizedException("User account is not active");
        }

        // Revoke the old refresh token (single-use rotation)
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        // Generate and persist new rotated refresh token
        String newRefreshTokenValue = jwtUtil.generateRefreshToken();

        RefreshToken newRefreshToken = RefreshToken.builder()
                .token(newRefreshTokenValue)
                .userId(user.getId())
                .expiresAt(
                        LocalDateTime.now()
                                .plus(java.time.Duration.ofMillis(refreshExpiration))
                )
                .build();

        refreshTokenRepository.save(newRefreshToken);

        return buildAuthenticationResponse(user, newRefreshTokenValue);
    }

    @Override
    public void logout(String refreshTokenValue) {

        RefreshToken refreshToken =
                refreshTokenRepository
                        .findByTokenAndRevokedFalse(refreshTokenValue)
                        .orElseThrow(() ->
                                new UnauthorizedException("Invalid refresh token"));

        refreshToken.setRevoked(true);

        refreshTokenRepository.save(refreshToken);
    }

    @Override
    public AuthenticationResponse changePassword(ChangePasswordRequest request) {

        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        User user = userRepository.findByEmailAndStatusNot(email, UserStatus.DELETED)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("New password must be different");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangeRequired(false);
        userRepository.save(user);

        refreshTokenRepository.deleteByUserId(user.getId());

        String refreshTokenValue = jwtUtil.generateRefreshToken();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .userId(user.getId())
                .expiresAt(
                        LocalDateTime.now()
                                .plus(java.time.Duration.ofMillis(refreshExpiration))
                )
                .build();

        refreshTokenRepository.save(refreshToken);

        return buildAuthenticationResponse(user, refreshTokenValue);
    }

    private AuthenticationResponse buildAuthenticationResponse(User user, String refreshTokenValue) {

        Role role = roleRepository
                .findById(user.getRoleId())
                .orElseThrow(() -> new RoleNotFoundException(user.getRoleId()));

        List<Permission> permissions = permissionRepository.findAllById(role.getPermissionIds());

        List<String> permissionNames = permissions.stream()
                .map(permission -> permission.getPermissionType().name().toLowerCase().replace("_", ":"))
                .toList();

        String token = jwtUtil.generateToken(
                user.getEmail(), 
                role.getName(), 
                permissionNames,
                user.isPasswordChangeRequired()
        );

        UserResponse userResponse = userMapper.toUserResponse(user, role.getName());

        return AuthenticationResponse.builder()
                .token(token)
                .type("Bearer")
                .refreshToken(refreshTokenValue)
                .passwordChangeRequired(user.isPasswordChangeRequired())
                .user(userResponse)
                .build();
    }
}
