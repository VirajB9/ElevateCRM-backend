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
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.core.context.SecurityContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
    private final MongoTemplate mongoTemplate;
    
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_MINUTES = 15;

    @Value("${app.jwt.refresh-expiration}")
    private long refreshExpiration;

    @Override
    public AuthenticationResponse login(LoginRequest request) {

        String email = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : null;

        User user = userRepository
                .findByEmailAndStatusNot(email, UserStatus.DELETED)
                .orElseThrow(() ->
                        new UnauthorizedException("Invalid email or password"));

        if (user.getLockoutUntil() != null) {
            if (user.getLockoutUntil().isAfter(LocalDateTime.now(ZoneOffset.UTC))) {
                throw new UnauthorizedException("Account is temporarily locked due to multiple failed login attempts. Please try again later.");
            } else {
                user.setFailedLoginAttempts(0);
                user.setLockoutUntil(null);
                mongoTemplate.updateFirst(
                        Query.query(Criteria.where("id").is(user.getId())),
                        new Update().set("failedLoginAttempts", 0).unset("lockoutUntil"),
                        User.class
                );
            }
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            User updatedUser = mongoTemplate.findAndModify(
                    Query.query(Criteria.where("id").is(user.getId())),
                    new Update().inc("failedLoginAttempts", 1),
                    FindAndModifyOptions.options().returnNew(true),
                    User.class
            );

            int attempts = (updatedUser != null) ? updatedUser.getFailedLoginAttempts() : (user.getFailedLoginAttempts() + 1);
            user.setFailedLoginAttempts(attempts);

            if (attempts >= MAX_FAILED_ATTEMPTS) {
                LocalDateTime lockoutUntil = LocalDateTime.now(ZoneOffset.UTC).plusMinutes(LOCKOUT_MINUTES);
                user.setLockoutUntil(lockoutUntil);
                mongoTemplate.updateFirst(
                        Query.query(Criteria.where("id").is(user.getId())),
                        new Update().set("lockoutUntil", lockoutUntil),
                        User.class
                );
            }
            throw new UnauthorizedException("Invalid email or password");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("User account is not active");
        }

        if (user.getFailedLoginAttempts() > 0 || user.getLockoutUntil() != null) {
            user.setFailedLoginAttempts(0);
            user.setLockoutUntil(null);
            mongoTemplate.updateFirst(
                    Query.query(Criteria.where("id").is(user.getId())),
                    new Update().set("failedLoginAttempts", 0).unset("lockoutUntil"),
                    User.class
            );
        }

        refreshTokenRepository.deleteByUserId(user.getId());

        String refreshTokenValue = jwtUtil.generateRefreshToken();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .userId(user.getId())
                .expiresAt(
                        LocalDateTime.now(ZoneOffset.UTC)
                                .plus(java.time.Duration.ofMillis(refreshExpiration))
                )
                .build();

        refreshTokenRepository.save(refreshToken);

        return buildAuthenticationResponse(user, refreshTokenValue);
    }

    @Override
    public AuthenticationResponse refresh(String refreshTokenValue) {

        if (refreshTokenValue == null || refreshTokenValue.isBlank()) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        String trimmedToken = refreshTokenValue.trim();

        // Atomic rotation: claim the active unrevoked token
        Query atomicQuery = Query.query(Criteria.where("token").is(trimmedToken).and("revoked").is(false));
        Update atomicUpdate = new Update()
                .set("revoked", true)
                .set("revokedAt", LocalDateTime.now(ZoneOffset.UTC));

        RefreshToken activeToken = mongoTemplate.findAndModify(
                atomicQuery,
                atomicUpdate,
                FindAndModifyOptions.options().returnNew(false),
                RefreshToken.class
        );

        if (activeToken == null) {
            RefreshToken existingToken = refreshTokenRepository.findByToken(trimmedToken)
                    .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

            if (existingToken.isRevoked()) {
                // Concurrency grace window (15 seconds) protects against multi-request session destruction
                if (existingToken.getRevokedAt() != null &&
                        existingToken.getRevokedAt().isAfter(LocalDateTime.now(ZoneOffset.UTC).minusSeconds(15))) {
                    throw new UnauthorizedException("Refresh token was recently rotated; please use the newly issued token");
                }
                // Compromised token reuse detected: revoke all active tokens for this user
                refreshTokenRepository.deleteByUserId(existingToken.getUserId());
                userRepository.findById(existingToken.getUserId()).ifPresent(user -> {
                    user.setTokensRevokedAt(LocalDateTime.now(ZoneOffset.UTC));
                    userRepository.save(user);
                });
                throw new UnauthorizedException("Refresh token was already revoked");
            }

            throw new UnauthorizedException("Invalid refresh token");
        }

        if (activeToken.getExpiresAt().isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
            throw new UnauthorizedException("Refresh token has expired");
        }

        User user = userRepository
                .findById(activeToken.getUserId())
                .orElseThrow(() ->
                        new UnauthorizedException("User not found"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("User account is not active");
        }

        // Generate and persist new rotated refresh token
        String newRefreshTokenValue = jwtUtil.generateRefreshToken();

        RefreshToken newRefreshToken = RefreshToken.builder()
                .token(newRefreshTokenValue)
                .userId(user.getId())
                .expiresAt(
                        LocalDateTime.now(ZoneOffset.UTC)
                                .plus(java.time.Duration.ofMillis(refreshExpiration))
                )
                .revoked(false)
                .build();

        refreshTokenRepository.save(newRefreshToken);

        return buildAuthenticationResponse(user, newRefreshTokenValue);
    }

    @Override
    public void logout(String refreshTokenValue) {

        LocalDateTime revocationTime = LocalDateTime.now(ZoneOffset.UTC)
                .truncatedTo(java.time.temporal.ChronoUnit.SECONDS);

        if (refreshTokenValue != null && !refreshTokenValue.isBlank()) {
            String trimmedToken = refreshTokenValue.trim();
            mongoTemplate.updateFirst(
                    Query.query(Criteria.where("token").is(trimmedToken).and("revoked").is(false)),
                    new Update().set("revoked", true).set("revokedAt", revocationTime),
                    RefreshToken.class
            );
            refreshTokenRepository.findByToken(trimmedToken).ifPresent(token -> {
                if (token.getUserId() != null) {
                    refreshTokenRepository.deleteByUserId(token.getUserId());
                    userRepository.findById(token.getUserId()).ifPresent(user -> {
                        user.setTokensRevokedAt(revocationTime);
                        userRepository.save(user);
                    });
                }
            });
        }

        com.viraj.dmabackend.auth.security.SecurityUtils.getCurrentUserEmail()
                .flatMap(email -> userRepository.findByEmailAndStatusNot(email, UserStatus.DELETED))
                .ifPresent(user -> {
                    refreshTokenRepository.deleteByUserId(user.getId());
                    user.setTokensRevokedAt(revocationTime);
                    userRepository.save(user);
                });
    }

    @Override
    public AuthenticationResponse changePassword(ChangePasswordRequest request) {

        String email = com.viraj.dmabackend.auth.security.SecurityUtils.getCurrentUserEmail()
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        User user = userRepository.findByEmailAndStatusNot(email, UserStatus.DELETED)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("User account is not active");
        }

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        if (request.getNewPassword() == null || !request.getNewPassword().equals(request.getNewPassword().trim())) {
            throw new BadRequestException("New password cannot contain leading or trailing whitespace");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("New password must be different");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangeRequired(false);
        user.setTokensRevokedAt(LocalDateTime.now(ZoneOffset.UTC).truncatedTo(java.time.temporal.ChronoUnit.SECONDS));
        userRepository.save(user);

        refreshTokenRepository.deleteByUserId(user.getId());

        String refreshTokenValue = jwtUtil.generateRefreshToken();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .userId(user.getId())
                .expiresAt(
                        LocalDateTime.now(ZoneOffset.UTC)
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

        List<String> permissionIds = role.getPermissionIds() != null
                ? role.getPermissionIds()
                : java.util.Collections.emptyList();

        List<Permission> permissions = permissionRepository.findAllById(permissionIds);

        List<String> permissionNames = permissions.stream()
                .filter(permission -> permission != null && permission.getPermissionType() != null)
                .map(permission -> permission.getPermissionType().name().toLowerCase().replace("_", ":"))
                .toList();

        String token;
        if (user.getTokensRevokedAt() != null) {
            long revokedEpochSecond = user.getTokensRevokedAt().toEpochSecond(ZoneOffset.UTC);
            long nowEpochSecond = System.currentTimeMillis() / 1000;
            if (nowEpochSecond <= revokedEpochSecond) {
                java.util.Date issuedAt = new java.util.Date((revokedEpochSecond + 1) * 1000L);
                token = jwtUtil.generateToken(
                        user.getEmail(),
                        role.getName(),
                        permissionNames,
                        user.isPasswordChangeRequired(),
                        issuedAt
                );
            } else {
                token = jwtUtil.generateToken(
                        user.getEmail(),
                        role.getName(),
                        permissionNames,
                        user.isPasswordChangeRequired()
                );
            }
        } else {
            token = jwtUtil.generateToken(
                    user.getEmail(),
                    role.getName(),
                    permissionNames,
                    user.isPasswordChangeRequired()
            );
        }

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
