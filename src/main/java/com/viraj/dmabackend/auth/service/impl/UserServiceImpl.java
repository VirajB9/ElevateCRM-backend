package com.viraj.dmabackend.auth.service.impl;

import com.viraj.dmabackend.auth.dto.*;
import com.viraj.dmabackend.auth.enums.UserStatus;
import com.viraj.dmabackend.auth.entity.Role;
import com.viraj.dmabackend.auth.entity.User;
import com.viraj.dmabackend.auth.exception.*;
import com.viraj.dmabackend.auth.mapper.UserMapper;
import com.viraj.dmabackend.auth.repository.RoleRepository;
import com.viraj.dmabackend.auth.repository.UserRepository;
import com.viraj.dmabackend.auth.security.CustomUserDetails;
import com.viraj.dmabackend.auth.service.UserService;
import com.viraj.dmabackend.auth.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.viraj.dmabackend.common.util.PasswordGenerator.generatePassword;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;
    
    private final UserMapper userMapper;
    
    private final UserValidator userValidator;

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

        Page<User> users = userRepository.findAll(pageable);
        return mapToUserResponsePage(users);
    }

    @Override
    public Page<UserResponse> searchUsers(String keyword, Pageable pageable) {

        Page<User> users = userRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        keyword,
                        keyword,
                        keyword,
                        pageable
                );
        return mapToUserResponsePage(users);
    }

    @Override
    public Page<UserResponse> filterUsersByStatus(UserStatus status, Pageable pageable) {

        Page<User> users = userRepository.findByStatus(status, pageable);
        return mapToUserResponsePage(users);
    }

    @Override
    public UserResponse updateUser(String userId, UpdateUserRequest request) {

        User user = findUserById(userId);

        userValidator.validatePhoneForUpdate(request.getPhoneNumber(), user.getId());

        Role role = findRoleById(request.getRoleId());
        User currentUser = findCurrentUser();
        Role currentUserRole = findRoleById(currentUser.getRoleId());

        userValidator.validateRoleAssignment(currentUser, currentUserRole, role);

        updateUserFields(user, request, role);

        User updatedUser = userRepository.save(user);
        return mapUser(updatedUser);
    }

    @Override
    public UserResponse updateUserStatus(String userId, UpdateUserStatusRequest request) {

        userValidator.validateStatusUpdate(request.getStatus());

        User user = findUserById(userId);
        user.setStatus(request.getStatus());

        User updatedUser = userRepository.save(user);
        return mapUser(updatedUser);
    }

    @Override
    public UserResponse softDeleteUser(String userId) {

        User user = findUserById(userId);
        user.setStatus(UserStatus.DELETED);

        User deletedUser = userRepository.save(user);
        return mapUser(deletedUser);
    }

    // =========================
    // Helper Methods
    // =========================
    private User findUserById(String userId) {

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException(userId));
    }

    private Role findRoleById(String roleId) {

        return roleRepository.findById(roleId)
                .orElseThrow(() ->
                        new RoleNotFoundException(roleId));
    }

    private User findCurrentUser() {

        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        return userDetails.getUser();
    }

    private User buildUser(CreateUserRequest request, Role role, String encodedPassword) {

        return User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(encodedPassword)
                .roleId(role.getId())
                .status(UserStatus.ACTIVE)
                .build();
    }

    private void updateUserFields(User user, UpdateUserRequest request, Role role) {

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRoleId(role.getId());
    }

    private Page<UserResponse> mapToUserResponsePage(Page<User> users) {

        return users.map(this::mapUser);
    }

    private UserResponse mapUser(User user) {

        Role role = findRoleById(user.getRoleId());
        return userMapper.toUserResponse(user, role.getName());
    }
}