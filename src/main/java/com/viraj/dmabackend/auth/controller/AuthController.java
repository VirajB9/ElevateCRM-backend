package com.viraj.dmabackend.auth.controller;

import com.viraj.dmabackend.auth.dto.AuthenticationResponse;
import com.viraj.dmabackend.auth.dto.LoginRequest;
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
}
