package com.viraj.dmabackend.auth.controller;

import com.viraj.dmabackend.auth.dto.*;
import com.viraj.dmabackend.auth.enums.UserStatus;
import com.viraj.dmabackend.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "2. User Management")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;

    @PreAuthorize("hasAuthority('user:create')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create User")
    public CreateUserResponse createUser(
            @Valid @RequestBody CreateUserRequest request) {

        return userService.createUser(request);
    }

    @PreAuthorize("hasAuthority('user:read')")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get All Users")
    public Page<UserResponse> getAllUsers(
            Pageable pageable) {

        return userService.getAllUsers(pageable);
    }

    @PreAuthorize("hasAuthority('user:read')")
    @GetMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get User By Id")
    public UserResponse getUserById(
            @PathVariable String userId) {

        return userService.getUserById(userId);
    }

    @PreAuthorize("hasAuthority('user:update')")
    @PutMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update User")
    public UserResponse updateUser(
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserRequest request) {

        return userService.updateUser(userId, request);
    }

    @PreAuthorize("hasAuthority('user:update')")
    @PatchMapping("/{userId}/status")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update User Status")
    public UserResponse updateUserStatus(
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserStatusRequest request) {

        return userService.updateUserStatus(userId, request);
    }

    @PreAuthorize("hasAuthority('user:delete')")
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Soft Delete User")
    public UserResponse softDeleteUser(
            @PathVariable String userId) {

        return userService.softDeleteUser(userId);
    }

    @PreAuthorize("hasAuthority('user:read')")
    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Search Users")
    public Page<UserResponse> searchUsers(
            @RequestParam String keyword,
            Pageable pageable) {

        return userService.searchUsers(keyword, pageable);
    }

    @PreAuthorize("hasAuthority('user:read')")
    @GetMapping("/filter")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Filter Users By Status")
    public Page<UserResponse> filterUsersByStatus(
            @RequestParam UserStatus status,
            Pageable pageable) {

        return userService.filterUsersByStatus(status, pageable);
    }
}