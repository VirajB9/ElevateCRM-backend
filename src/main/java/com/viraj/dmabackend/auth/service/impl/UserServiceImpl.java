package com.viraj.dmabackend.auth.service.impl;

import com.viraj.dmabackend.auth.dto.*;
import com.viraj.dmabackend.auth.enums.UserStatus;
import com.viraj.dmabackend.auth.entity.Role;
import com.viraj.dmabackend.auth.entity.User;
import com.viraj.dmabackend.auth.exception.*;
import com.viraj.dmabackend.auth.repository.RoleRepository;
import com.viraj.dmabackend.auth.repository.UserRepository;
import com.viraj.dmabackend.auth.security.CustomUserDetails;
import com.viraj.dmabackend.auth.service.UserService;
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

    @Override
    public CreateUserResponse createUser(CreateUserRequest request) {

        validateDuplicateEmail(request.getEmail());

        validateDuplicatePhone(request.getPhoneNumber());

        User currentUser = findCurrentUser();

        Role role = findRoleById(request.getRoleId());

        validateRoleAssignment(currentUser, role);

        String generatedPassword = generatePassword();

        String encodedPassword = passwordEncoder.encode(generatedPassword);

        User user = buildUser(request, role, encodedPassword);

        User savedUser = userRepository.save(user);

        return CreateUserResponse.builder()
                .user(mapToUserResponse(savedUser, role))
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

        validatePhoneForUpdate(request.getPhoneNumber(), user.getId());

        Role role = findRoleById(request.getRoleId());

        User currentUser = findCurrentUser();

        validateRoleAssignment(currentUser, role);

        updateUserFields(user, request, role);

        User updatedUser = userRepository.save(user);

        return mapUser(updatedUser);
    }

    @Override
    public UserResponse updateUserStatus(String userId, UpdateUserStatusRequest request) {

        validateStatusUpdate(request.getStatus());

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


    //Helper Methods
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

    private void validateDuplicateEmail(String email) {

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }
    }

    private void validateDuplicatePhone(String phoneNumber) {

        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new DuplicatePhoneException(phoneNumber);
        }
    }

    private void validatePhoneForUpdate(String phoneNumber, String userId) {

        if (userRepository.existsByPhoneNumberAndIdNot(phoneNumber, userId)) {
            throw new DuplicatePhoneException(phoneNumber);
        }
    }

    private void validateStatusUpdate(UserStatus status) {

        if (status == UserStatus.DELETED) {
            throw new InvalidUserStatusException(status);
        }
    }

    //Validates whether the authenticated user is allowed to assign the requested role.
    private void validateRoleAssignment(User currentUser, Role targetRole) {

        Role currentUserRole = findRoleById(currentUser.getRoleId());

        String currentRole = currentUserRole.getName();

        String targetRoleName = targetRole.getName();

        switch (currentRole) {
            case "OWNER":
                return;

            case "MANAGER":
                if (!targetRoleName.equals("EMPLOYEE")
                        && !targetRoleName.equals("INTERN")) {
                    throw new UnauthorizedRoleAssignmentException("Managers can only create Employees or Interns");
                }
                return;

            case "EMPLOYEE":
            case "INTERN":
                throw new UnauthorizedRoleAssignmentException("You are not allowed to create users");

            default:
                throw new UnauthorizedRoleAssignmentException("Invalid role assignment");
        }
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

    private UserResponse mapToUserResponse(User user, Role role) {

        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .roleName(role.getName())
                .status(user.getStatus())
                .build();
    }

    private Page<UserResponse> mapToUserResponsePage(Page<User> users) {

        return users.map(this::mapUser);
    }

    private UserResponse mapUser(User user) {

        Role role = findRoleById(user.getRoleId());

        return mapToUserResponse(user, role);
    }
}