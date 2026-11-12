package com.viraj.dmabackend.auth.service.impl;

import com.viraj.dmabackend.auth.dto.*;
import com.viraj.dmabackend.auth.enums.UserStatus;
import com.viraj.dmabackend.auth.entity.Role;
import com.viraj.dmabackend.auth.entity.User;
import com.viraj.dmabackend.auth.exception.*;
import com.viraj.dmabackend.auth.mapper.UserMapper;
import com.viraj.dmabackend.auth.repository.RefreshTokenRepository;
import com.viraj.dmabackend.auth.repository.RoleRepository;
import com.viraj.dmabackend.auth.repository.UserRepository;
import com.viraj.dmabackend.auth.security.CustomUserDetails;
import com.viraj.dmabackend.auth.service.RoleService;
import com.viraj.dmabackend.auth.service.UserService;
import com.viraj.dmabackend.auth.validator.UserValidator;
import com.viraj.dmabackend.common.util.PaginationUtils;
import com.viraj.dmabackend.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.viraj.dmabackend.common.util.PasswordGenerator.generatePassword;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final RoleService roleService;

    private final PasswordEncoder passwordEncoder;

    private final UserMapper userMapper;

    private final UserValidator userValidator;

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    @Override
    public CreateUserResponse createUser(CreateUserRequest request) {

        userValidator.validateDuplicateEmail(request.getEmail());
        userValidator.validateDuplicatePhone(request.getPhoneNumber());

        User currentUser = findCurrentUser();

        Role role = findRoleById(request.getRoleId());
        Role currentUserRole = findRoleById(currentUser.getRoleId());

        userValidator.validateRoleAssignment(currentUser, currentUserRole, role);

        String generatedPassword = generatePassword();
        String encodedPassword = passwordEncoder.encode(generatedPassword);

        User user = buildUser(request, role, encodedPassword);
        User savedUser = userRepository.save(user);

        return CreateUserResponse.builder()
                .user(userMapper.toUserResponse(savedUser, role.getName()))
                .temporaryPassword(generatedPassword)
                .build();
    }

    @Override
    public UserResponse getUserById(String userId) {

        User user = findUserById(userId);
        return mapUser(user);
    }

    @Override
    public Page<UserResponse> getAllUsers(Pageable pageable) {

        Page<User> users = userRepository.findByStatusNot(UserStatus.DELETED, PaginationUtils.clampPageable(pageable));
        return mapToUserResponsePage(users);
    }

    @Override
    public Page<UserResponse> searchUsers(String keyword, Pageable pageable) {

        Page<User> users = userRepository.searchUsers(keyword, PaginationUtils.clampPageable(pageable));
        return mapToUserResponsePage(users);
    }

    @Override
    public Page<UserResponse> filterUsersByStatus(UserStatus status, Pageable pageable) {

        if (status == UserStatus.DELETED) {
            throw new BadRequestException("Querying soft-deleted users is not permitted");
        }

        Page<User> users = userRepository.findByStatus(status, PaginationUtils.clampPageable(pageable));
        return mapToUserResponsePage(users);
    }

    @Transactional
    @Override
    public UserResponse updateUser(String userId, UpdateUserRequest request) {

        User user = findUserById(userId);

        userValidator.validatePhoneForUpdate(request.getPhoneNumber(), user.getId());

        Role targetRole = findRoleById(user.getRoleId());
        Role newRole = findRoleById(request.getRoleId());
        User currentUser = findCurrentUser();
        Role currentUserRole = findRoleById(currentUser.getRoleId());

        if ("OWNER".equals(targetRole.getName()) && !"OWNER".equals(currentUserRole.getName())) {
            throw new UnauthorizedRoleAssignmentException("Only an Owner can modify the Owner account");
        }

        if ("OWNER".equals(targetRole.getName()) && !"OWNER".equals(newRole.getName())) {
            throw new BadRequestException("The Owner account cannot be demoted");
        }

        boolean isSelf = currentUser.getId() != null && currentUser.getId().equals(user.getId());

        if (isSelf && !targetRole.getId().equals(newRole.getId())) {
            throw new BadRequestException("Users cannot change their own role");
        }

        userValidator.validateUserManagement(currentUser, currentUserRole, user, targetRole);

        if (!isSelf || !targetRole.getId().equals(newRole.getId())) {
            userValidator.validateRoleAssignment(currentUser, currentUserRole, newRole);
        }

        boolean roleChanged = !targetRole.getId().equals(newRole.getId());
        updateUserFields(user, request, newRole);

        if (roleChanged) {
            user.setTokensRevokedAt(LocalDateTime.now(ZoneOffset.UTC));
            refreshTokenRepository.deleteByUserId(user.getId());
        }

        User updatedUser = userRepository.save(user);
        return mapUser(updatedUser);
    }

    @Transactional
    @Override
    public UserResponse updateUserStatus(String userId, UpdateUserStatusRequest request) {

        userValidator.validateStatusUpdate(request.getStatus());

        User user = findUserById(userId);
        Role targetRole = findRoleById(user.getRoleId());
        if ("OWNER".equals(targetRole.getName())) {
            throw new BadRequestException("The system Owner account status cannot be modified");
        }

        User currentUser = findCurrentUser();
        if (currentUser.getId().equals(user.getId())) {
            throw new BadRequestException("Users cannot modify their own account status");
        }

        Role currentUserRole = findRoleById(currentUser.getRoleId());
        userValidator.validateUserManagement(currentUser, currentUserRole, user, targetRole);

        user.setStatus(request.getStatus());

        if (request.getStatus() == UserStatus.ACTIVE) {
            user.setLockoutUntil(null);
            user.setFailedLoginAttempts(0);
        } else {
            user.setTokensRevokedAt(LocalDateTime.now(ZoneOffset.UTC));
            refreshTokenRepository.deleteByUserId(user.getId());
        }

        User updatedUser = userRepository.save(user);
        return mapUser(updatedUser);
    }

    @Transactional
    @Override
    public UserResponse softDeleteUser(String userId) {

        User user = findUserById(userId);
        Role targetRole = findRoleById(user.getRoleId());
        if ("OWNER".equals(targetRole.getName())) {
            throw new BadRequestException("The system Owner account cannot be deleted");
        }

        User currentUser = findCurrentUser();
        if (currentUser.getId().equals(user.getId())) {
            throw new BadRequestException("Users cannot delete their own account");
        }

        Role currentUserRole = findRoleById(currentUser.getRoleId());
        userValidator.validateUserManagement(currentUser, currentUserRole, user, targetRole);

        userValidator.validateUserDeletable(user.getId());

        user.setStatus(UserStatus.DELETED);
        user.setDeleted(true);
        user.setTokensRevokedAt(LocalDateTime.now(ZoneOffset.UTC));

        refreshTokenRepository.deleteByUserId(user.getId());

        User deletedUser = userRepository.save(user);
        return mapUser(deletedUser);
    }

    // =========================
    // Helper Methods
    // =========================
    private User findUserById(String userId) {
        String trimmedUserId = userId != null ? userId.trim() : null;
        return userRepository.findByIdAndStatusNot(trimmedUserId, UserStatus.DELETED)
                .orElseThrow(() ->
                        new UserNotFoundException(userId));
    }

    private Role findRoleById(String roleId) {
        String trimmedRoleId = roleId != null ? roleId.trim() : null;
        return roleService.findRoleEntityById(trimmedRoleId);
    }

    private User findCurrentUser() {

        String email = com.viraj.dmabackend.auth.security.SecurityUtils.getCurrentUserEmail()
                .orElseThrow(() -> new com.viraj.dmabackend.exception.UnauthorizedException("User not found"));
                
        return userRepository.findByEmailAndStatusNot(email, UserStatus.DELETED)
                .orElseThrow(() -> new com.viraj.dmabackend.exception.UnauthorizedException("User not found"));
    }

    private User buildUser(CreateUserRequest request, Role role, String encodedPassword) {

        return User.builder()
                .firstName(request.getFirstName() != null ? request.getFirstName().trim() : null)
                .lastName(request.getLastName() != null ? request.getLastName().trim() : null)
                .email(request.getEmail() != null ? request.getEmail().trim().toLowerCase() : null)
                .phoneNumber(request.getPhoneNumber() != null ? request.getPhoneNumber().trim() : null)
                .password(encodedPassword)
                .passwordChangeRequired(true)
                .roleId(role.getId())
                .status(UserStatus.ACTIVE)
                .build();
    }

    private void updateUserFields(User user, UpdateUserRequest request, Role role) {

        user.setFirstName(request.getFirstName() != null ? request.getFirstName().trim() : null);
        user.setLastName(request.getLastName() != null ? request.getLastName().trim() : null);
        user.setPhoneNumber(request.getPhoneNumber() != null ? request.getPhoneNumber().trim() : null);
        user.setRoleId(role.getId());
    }

    private Page<UserResponse> mapToUserResponsePage(
            Page<User> users) {

        List<String> roleIds = users.getContent()
                .stream()
                .map(User::getRoleId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();

        Map<String, Role> rolesById = roleIds.isEmpty()
                ? java.util.Collections.emptyMap()
                : roleRepository
                        .findAllById(roleIds)
                        .stream()
                        .filter(java.util.Objects::nonNull)
                        .collect(Collectors.toMap(
                                Role::getId,
                                Function.identity(),
                                (a, b) -> a));

        return users.map(user -> {

            Role role = user.getRoleId() != null ? rolesById.get(user.getRoleId()) : null;
            String roleName = role != null && role.getName() != null ? role.getName() : "UNKNOWN";

            return userMapper.toUserResponse(
                    user,
                    roleName);
        });
    }

    private UserResponse mapUser(User user) {
        Role role = null;
        if (user.getRoleId() != null) {
            try {
                role = roleService.findRoleEntityById(user.getRoleId().trim());
            } catch (RoleNotFoundException ignored) {
                // Fallback to UNKNOWN if role record was removed
            }
        }
        String roleName = role != null && role.getName() != null ? role.getName() : "UNKNOWN";
        return userMapper.toUserResponse(user, roleName);
    }
}
