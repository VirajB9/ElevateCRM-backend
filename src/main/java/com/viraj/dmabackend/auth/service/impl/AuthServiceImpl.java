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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
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

    private static final String DUMMY_BCRYPT_HASH = "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi";

    @Override
    public AuthenticationResponse login(LoginRequest request) {

        String email = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : null;

        User user = userRepository
                .findByEmailAndStatusNot(email, UserStatus.DELETED)
                .orElse(null);

        if (user == null) {
            passwordEncoder.matches(request.getPassword() != null ? request.getPassword() : "", DUMMY_BCRYPT_HASH);
            throw new UnauthorizedException("Invalid email or password");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("Invalid email or password");
        }

        if (user.getLockoutUntil() != null) {
            if (user.getLockoutUntil().isAfter(LocalDateTime.now(ZoneOffset.UTC))) {
                throw new UnauthorizedException("Invalid email or password");
            }
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
            boolean wasLockedExpired = user.getLockoutUntil() != null && !user.getLockoutUntil().isAfter(now);

            Update update = wasLockedExpired
                    ? new Update().set("failedLoginAttempts", 1).unset("lockoutUntil").set("updatedAt", now).set("updatedBy", "SYSTEM").inc("version", 1)
                    : new Update().inc("failedLoginAttempts", 1).set("updatedAt", now).set("updatedBy", "SYSTEM").inc("version", 1);

            User updatedUser = mongoTemplate.findAndModify(
                    Query.query(Criteria.where("id").is(user.getId())),
                    update,
                    FindAndModifyOptions.options().returnNew(true),
                    User.class
            );

            int attempts = (updatedUser != null) ? updatedUser.getFailedLoginAttempts() : (wasLockedExpired ? 1 : user.getFailedLoginAttempts() + 1);
            user.setFailedLoginAttempts(attempts);
            if (wasLockedExpired) {
                user.setLockoutUntil(null);
            }

            if (attempts >= MAX_FAILED_ATTEMPTS) {
                LocalDateTime lockoutUntil = now.plusMinutes(LOCKOUT_MINUTES);
                LocalDateTime revocationTime = now;
                user.setLockoutUntil(lockoutUntil);
                user.setTokensRevokedAt(revocationTime);
                mongoTemplate.updateFirst(
                        Query.query(Criteria.where("id").is(user.getId())),
                        new Update().set("lockoutUntil", lockoutUntil)
                                .set("tokensRevokedAt", revocationTime)
                                .set("updatedAt", now)
                                .set("updatedBy", "SYSTEM")
                                .inc("version", 1),
                        User.class
                );
            }
            throw new UnauthorizedException("Invalid email or password");
        }

        // Verify user account was not locked out during BCrypt verification
        User freshUser = userRepository.findById(user.getId()).orElse(user);
        if (freshUser.getLockoutUntil() != null && freshUser.getLockoutUntil().isAfter(LocalDateTime.now(ZoneOffset.UTC))) {
            throw new UnauthorizedException("Invalid email or password");
        }

        if (user.getFailedLoginAttempts() > 0 || user.getLockoutUntil() != null) {
            user.setFailedLoginAttempts(0);
            user.setLockoutUntil(null);
            mongoTemplate.updateFirst(
                    Query.query(Criteria.where("id").is(user.getId())),
                    new Update().set("failedLoginAttempts", 0)
                            .unset("lockoutUntil")
                            .set("updatedAt", LocalDateTime.now(ZoneOffset.UTC))
                            .set("updatedBy", "SYSTEM")
                            .inc("version", 1),
                    User.class
            );
        }

        // Multi-device session support: clean up expired tokens for the user rather than wiping other active sessions
        refreshTokenRepository.deleteByUserIdAndExpiresAtBefore(user.getId(), LocalDateTime.now(ZoneOffset.UTC));

        String refreshTokenValue = jwtUtil.generateRefreshToken();
        String tokenHash = RefreshToken.hashToken(refreshTokenValue);

        RefreshToken refreshToken = RefreshToken.builder()
                .tokenHash(tokenHash)
                .userId(user.getId())
                .expiresAt(
                        LocalDateTime.now(ZoneOffset.UTC)
                                .plus(java.time.Duration.ofMillis(refreshExpiration))
                )
                .build();

        refreshTokenRepository.save(refreshToken);

        return buildAuthenticationResponse(user, refreshTokenValue);
    }

    @Transactional
    @Override
    public AuthenticationResponse refresh(String refreshTokenValue) {

        if (refreshTokenValue == null || refreshTokenValue.isBlank()) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        String trimmedToken = refreshTokenValue.trim();
        String tokenHash = RefreshToken.hashToken(trimmedToken);

        // Atomic rotation: claim the active unrevoked token
        Query atomicQuery = Query.query(Criteria.where("tokenHash").is(tokenHash).and("revoked").is(false));
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
            RefreshToken existingToken = refreshTokenRepository.findByTokenHash(tokenHash)
                    .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

            if (existingToken.isRevoked()) {
                if (existingToken.getExpiresAt() != null && existingToken.getExpiresAt().isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
                    throw new UnauthorizedException("Refresh token has expired");
                }
                // Concurrency grace window (15 seconds) protects against multi-request session destruction and dropped responses
                if (existingToken.getRevokedAt() != null &&
                        existingToken.getRevokedAt().isAfter(LocalDateTime.now(ZoneOffset.UTC).minusSeconds(15))) {
                    User user = userRepository.findByIdAndStatusNot(existingToken.getUserId(), UserStatus.DELETED)
                            .filter(u -> !u.isDeleted() && u.getStatus() == UserStatus.ACTIVE)
                            .orElseThrow(() -> new UnauthorizedException("User not found"));
                    String replacementTokenValue = jwtUtil.generateRefreshToken();
                    String replacementHash = RefreshToken.hashToken(replacementTokenValue);
                    RefreshToken replacementToken = RefreshToken.builder()
                            .tokenHash(replacementHash)
                            .userId(user.getId())
                            .expiresAt(LocalDateTime.now(ZoneOffset.UTC).plus(java.time.Duration.ofMillis(refreshExpiration)))
                            .revoked(false)
                            .build();
                    refreshTokenRepository.save(replacementToken);
                    return buildAuthenticationResponse(user, replacementTokenValue);
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
                .findByIdAndStatusNot(activeToken.getUserId(), UserStatus.DELETED)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() ->
                        new UnauthorizedException("User not found"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("User account is not active");
        }

        if (user.getTokensRevokedAt() != null) {
            if (activeToken.getCreatedAt() == null || !activeToken.getCreatedAt().isAfter(user.getTokensRevokedAt())) {
                throw new UnauthorizedException("Session has been revoked");
            }
        }

        // Generate and persist new rotated refresh token
        String newRefreshTokenValue = jwtUtil.generateRefreshToken();
        String newTokenHash = RefreshToken.hashToken(newRefreshTokenValue);

        RefreshToken newRefreshToken = RefreshToken.builder()
                .tokenHash(newTokenHash)
                .userId(user.getId())
                .expiresAt(
                        LocalDateTime.now(ZoneOffset.UTC)
                                .plus(java.time.Duration.ofMillis(refreshExpiration))
                )
                .revoked(false)
                .build();

        refreshTokenRepository.save(newRefreshToken);

        mongoTemplate.updateFirst(
                Query.query(Criteria.where("tokenHash").is(tokenHash)),
                new Update().set("replacedByToken", newTokenHash)
                        .set("updatedAt", LocalDateTime.now(ZoneOffset.UTC))
                        .set("updatedBy", user.getEmail() != null ? user.getEmail() : "SYSTEM"),
                RefreshToken.class
        );

        return buildAuthenticationResponse(user, newRefreshTokenValue);
    }

    @Override
    public void logout(String refreshTokenValue) {

        LocalDateTime revocationTime = LocalDateTime.now(ZoneOffset.UTC);

        if (refreshTokenValue != null && !refreshTokenValue.isBlank()) {
            String trimmedToken = refreshTokenValue.trim();
            String tokenHash = RefreshToken.hashToken(trimmedToken);
            mongoTemplate.updateFirst(
                    Query.query(Criteria.where("tokenHash").is(tokenHash).and("revoked").is(false)),
                    new Update().set("revoked", true).set("revokedAt", revocationTime),
                    RefreshToken.class
            );
            refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
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

    @Transactional
    @Override
    public AuthenticationResponse changePassword(ChangePasswordRequest request) {

        String email = com.viraj.dmabackend.auth.security.SecurityUtils.getCurrentUserEmail()
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        User user = userRepository.findByEmailAndStatusNot(email, UserStatus.DELETED)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("User account is not active");
        }

        if (user.getLockoutUntil() != null) {
            if (user.getLockoutUntil().isAfter(LocalDateTime.now(ZoneOffset.UTC))) {
                throw new UnauthorizedException("Account is temporarily locked. Try again later.");
            }
        }

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
            boolean wasLockedExpired = user.getLockoutUntil() != null && !user.getLockoutUntil().isAfter(now);

            Update update = wasLockedExpired
                    ? new Update().set("failedLoginAttempts", 1).unset("lockoutUntil").set("updatedAt", now).set("updatedBy", "SYSTEM").inc("version", 1)
                    : new Update().inc("failedLoginAttempts", 1).set("updatedAt", now).set("updatedBy", "SYSTEM").inc("version", 1);

            User updatedUser = mongoTemplate.findAndModify(
                    Query.query(Criteria.where("id").is(user.getId())),
                    update,
                    FindAndModifyOptions.options().returnNew(true),
                    User.class
            );

            int attempts = (updatedUser != null) ? updatedUser.getFailedLoginAttempts() : (wasLockedExpired ? 1 : user.getFailedLoginAttempts() + 1);
            user.setFailedLoginAttempts(attempts);
            if (wasLockedExpired) {
                user.setLockoutUntil(null);
            }

            if (attempts >= MAX_FAILED_ATTEMPTS) {
                LocalDateTime lockoutUntil = now.plusMinutes(LOCKOUT_MINUTES);
                LocalDateTime revocationTime = now;
                user.setLockoutUntil(lockoutUntil);
                user.setTokensRevokedAt(revocationTime);
                mongoTemplate.updateFirst(
                        Query.query(Criteria.where("id").is(user.getId())),
                        new Update().set("lockoutUntil", lockoutUntil)
                                .set("tokensRevokedAt", revocationTime)
                                .set("updatedAt", now)
                                .set("updatedBy", "SYSTEM")
                                .inc("version", 1),
                        User.class
                );
            }
            throw new BadRequestException("Current password is incorrect");
        }

        if (user.getFailedLoginAttempts() > 0 || user.getLockoutUntil() != null) {
            user.setFailedLoginAttempts(0);
            user.setLockoutUntil(null);
            mongoTemplate.updateFirst(
                    Query.query(Criteria.where("id").is(user.getId())),
                    new Update().set("failedLoginAttempts", 0)
                            .unset("lockoutUntil")
                            .set("updatedAt", LocalDateTime.now(ZoneOffset.UTC))
                            .set("updatedBy", "SYSTEM"),
                    User.class
            );
        }

        if (request.getNewPassword() == null || !request.getNewPassword().equals(request.getNewPassword().trim())) {
            throw new BadRequestException("New password cannot contain leading or trailing whitespace");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("New password must be different");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangeRequired(false);
        user.setTokensRevokedAt(LocalDateTime.now(ZoneOffset.UTC).minusSeconds(1));
        userRepository.save(user);

        refreshTokenRepository.deleteByUserId(user.getId());

        String refreshTokenValue = jwtUtil.generateRefreshToken();
        String tokenHash = RefreshToken.hashToken(refreshTokenValue);

        RefreshToken refreshToken = RefreshToken.builder()
                .tokenHash(tokenHash)
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
            long revokedEpochMilli = user.getTokensRevokedAt().toInstant(ZoneOffset.UTC).toEpochMilli();
            long nowEpochMilli = System.currentTimeMillis();
            long targetIssuedAtMilli = Math.max(nowEpochMilli, revokedEpochMilli + 1000L);
            if (targetIssuedAtMilli > nowEpochMilli + 50L) {
                targetIssuedAtMilli = nowEpochMilli;
            }
            java.util.Date issuedAt = new java.util.Date(targetIssuedAtMilli);
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
