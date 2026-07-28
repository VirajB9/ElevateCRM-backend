package com.viraj.dmabackend.auth.service;

import com.viraj.dmabackend.auth.dto.*;
import com.viraj.dmabackend.auth.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface     UserService {
    CreateUserResponse createUser(CreateUserRequest request);

    UserResponse getUserById(String userId);

    Page<UserResponse> getAllUsers(Pageable pageable);

    Page<UserResponse> searchUsers(String keyword, Pageable pageable);

    Page<UserResponse> filterUsersByStatus(UserStatus status, Pageable pageable);

    UserResponse updateUser(String userId, UpdateUserRequest request);

    UserResponse updateUserStatus(String userId, UpdateUserStatusRequest request);

    UserResponse softDeleteUser(String userId);
}
