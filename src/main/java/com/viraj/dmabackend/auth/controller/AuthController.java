package com.viraj.dmabackend.auth.controller;

import com.viraj.dmabackend.auth.dto.AuthenticationResponse;
import com.viraj.dmabackend.auth.dto.LoginRequest;
import com.viraj.dmabackend.auth.dto.LogoutRequest;
import com.viraj.dmabackend.auth.dto.RefreshTokenRequest;
import com.viraj.dmabackend.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "1. Authentication", description = "Endpoints for user authentication and token generation")
@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "User login",
            description = "Authenticates a user using the provided credentials and returns a JWT authentication response.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User authenticated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid login request formatting"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public AuthenticationResponse login(
            @Parameter(description = "Login credentials (email and password)", required = true)
            @Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @Operation(
            summary = "Refresh token",
            description = "Issues a new JWT access token using a valid refresh token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request formatting"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    @PostMapping("/refresh")
    public AuthenticationResponse refresh(
            @Parameter(description = "Refresh token request payload", required = true)
            @Valid @RequestBody RefreshTokenRequest request) {

        return authService.refresh(
                request.getRefreshToken());
    }

    @Operation(
            summary = "User logout",
            description = "Revokes the provided refresh token to prevent further access token generation.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User logged out successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request formatting"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    @PostMapping("/logout")
    public void logout(
            @Parameter(description = "Logout request payload containing the refresh token", required = true)
            @Valid @RequestBody LogoutRequest request) {

        authService.logout(
                request.getRefreshToken());
    }
}
