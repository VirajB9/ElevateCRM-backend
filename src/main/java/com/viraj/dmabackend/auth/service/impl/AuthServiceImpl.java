package com.viraj.dmabackend.auth.service.impl;

import com.viraj.dmabackend.auth.dto.AuthenticationResponse;
import com.viraj.dmabackend.auth.dto.LoginRequest;
import com.viraj.dmabackend.auth.dto.UserResponse;
import com.viraj.dmabackend.auth.entity.Permission;
import com.viraj.dmabackend.auth.entity.RefreshToken;
import com.viraj.dmabackend.auth.entity.Role;
import com.viraj.dmabackend.auth.entity.User;
import com.viraj.dmabackend.auth.exception.RoleNotFoundException;
import com.viraj.dmabackend.auth.mapper.UserMapper;
import com.viraj.dmabackend.auth.repository.PermissionRepository;
import com.viraj.dmabackend.auth.repository.RefreshTokenRepository;
import com.viraj.dmabackend.auth.repository.RoleRepository;
import com.viraj.dmabackend.auth.repository.UserRepository;
import com.viraj.dmabackend.auth.security.JwtUtil;
import com.viraj.dmabackend.auth.service.AuthService;
import com.viraj.dmabackend.exception.UnauthorizedException;
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
    @Value("${app.jwt.refresh-expiration}")
    private long refreshExpiration;

    @Override
    public AuthenticationResponse login(LoginRequest request) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        if (user.getStatus() != com.viraj.dmabackend.auth.enums.UserStatus.ACTIVE) {
            throw new UnauthorizedException("User account is not active");
        }

        Role role = roleRepository
                .findById(user.getRoleId())
                .orElseThrow(() ->
                        new RoleNotFoundException(user.getRoleId()));

        List<Permission> permissions =
                permissionRepository.findAllById(role.getPermissionIds());

        List<String> permissionNames =
                permissions.stream()
                        .map(permission ->
                                permission.getPermissionType()
                                        .name()
                                        .toLowerCase()
                                        .replace("_", ":")
                        )
                        .toList();

        UserResponse userResponse =
                userMapper.toUserResponse(
                        user,
                        role.getName());

        String token = jwtUtil.generateToken(
                user.getEmail(),
                role.getName(),
                permissionNames);

        String refreshTokenValue =
                jwtUtil.generateRefreshToken();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .userId(user.getId())
                .expiresAt(
                        LocalDateTime.now()
                                .plus(java.time.Duration.ofMillis(refreshExpiration))
                )
                .build();

        refreshTokenRepository.save(refreshToken);

        return AuthenticationResponse.builder()
                .token(token)
                .type("Bearer")
                .refreshToken(refreshTokenValue)
                .user(userResponse)
                .build();
    }

    @Override
    public AuthenticationResponse refresh(String refreshTokenValue) {

        RefreshToken refreshToken =
                refreshTokenRepository
                        .findByTokenAndRevokedFalse(refreshTokenValue)
                        .orElseThrow(() ->
                                new UnauthorizedException(
                                        "Invalid refresh token"));

        if (refreshToken.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            throw new UnauthorizedException(
                    "Refresh token has expired");
        }

        User user = userRepository
                .findById(refreshToken.getUserId())
                .orElseThrow(() ->
                        new UnauthorizedException(
                                "User not found"));

        Role role = roleRepository
                .findById(user.getRoleId())
                .orElseThrow(() ->
                        new RoleNotFoundException(
                                user.getRoleId()));

        List<Permission> permissions =
                permissionRepository.findAllById(
                        role.getPermissionIds());

        List<String> permissionNames =
                permissions.stream()
                        .map(permission ->
                                permission.getPermissionType()
                                        .name()
                                        .toLowerCase()
                                        .replace("_", ":")
                        )
                        .toList();

        String token = jwtUtil.generateToken(
                user.getEmail(),
                role.getName(),
                permissionNames);

        UserResponse userResponse =
                userMapper.toUserResponse(
                        user,
                        role.getName());

        return AuthenticationResponse.builder()
                .token(token)
                .type("Bearer")
                .refreshToken(refreshTokenValue)
                .user(userResponse)
                .build();
    }

    @Override
    public void logout(String refreshTokenValue) {

        RefreshToken refreshToken =
                refreshTokenRepository
                        .findByTokenAndRevokedFalse(refreshTokenValue)
                        .orElseThrow(() ->
                                new UnauthorizedException(
                                        "Invalid refresh token"));

        refreshToken.setRevoked(true);

        refreshTokenRepository.save(refreshToken);
    }
}
