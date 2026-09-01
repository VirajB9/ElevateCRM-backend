package com.viraj.dmabackend.auth.controller;

import com.viraj.dmabackend.auth.dto.*;
import com.viraj.dmabackend.auth.enums.UserStatus;
import com.viraj.dmabackend.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "2. User Management", description = "Endpoints for managing users, roles, and statuses")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;

    @PreAuthorize("hasAuthority('user:create')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create User", description = "Creates a new user with a specific role. Requires 'user:create' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation failed"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public CreateUserResponse createUser(
            @Valid @RequestBody CreateUserRequest request) {
        return userService.createUser(request);
    }

    @PreAuthorize("hasAuthority('user:read')")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get All Users", description = "Retrieves a paginated list of all users. Requires 'user:read' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public Page<UserResponse> getAllUsers(
            @Parameter(description = "Pagination and sorting parameters (page, size, sort)") Pageable pageable) {
        return userService.getAllUsers(pageable);
    }

    @PreAuthorize("hasAuthority('user:read')")
    @GetMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get User By Id", description = "Retrieves a specific user's details using their unique ID. Requires 'user:read' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found and returned"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public UserResponse getUserById(
            @Parameter(description = "Unique identifier of the user", required = true)
            @PathVariable String userId) {
        return userService.getUserById(userId);
    }

    @PreAuthorize("hasAuthority('user:update')")
    @PutMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update User", description = "Updates a user's basic information. Requires 'user:update' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public UserResponse updateUser(
            @Parameter(description = "Unique identifier of the user", required = true)
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserRequest request) {
        return userService.updateUser(userId, request);
    }

    @PreAuthorize("hasAuthority('user:update')")
    @PatchMapping("/{userId}/status")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update User Status", description = "Changes the status (e.g., ACTIVE, INACTIVE) of a user. Requires 'user:update' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User status updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public UserResponse updateUserStatus(
            @Parameter(description = "Unique identifier of the user", required = true)
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        return userService.updateUserStatus(userId, request);
    }

    @PreAuthorize("hasAuthority('user:delete')")
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Soft Delete User", description = "Archives a user without physically deleting them from the database. Requires 'user:delete' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User successfully soft-deleted"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public UserResponse softDeleteUser(
            @Parameter(description = "Unique identifier of the user", required = true)
            @PathVariable String userId) {
        return userService.softDeleteUser(userId);
    }

    @PreAuthorize("hasAuthority('user:read')")
    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Search Users", description = "Searches users by matching keyword against name or email. Requires 'user:read' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search results returned successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public Page<UserResponse> searchUsers(
            @Parameter(description = "Keyword to search for in first name, last name, or email", required = true)
            @RequestParam String keyword,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        return userService.searchUsers(keyword, pageable);
    }

    @PreAuthorize("hasAuthority('user:read')")
    @GetMapping("/filter")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Filter Users By Status", description = "Retrieves a paginated list of users filtered by their exact status. Requires 'user:read' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Filtered users returned successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public Page<UserResponse> filterUsersByStatus(
            @Parameter(description = "Exact user status to filter by", required = true)
            @RequestParam UserStatus status,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        return userService.filterUsersByStatus(status, pageable);
    }
}
