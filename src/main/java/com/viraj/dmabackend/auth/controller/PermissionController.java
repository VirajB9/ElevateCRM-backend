package com.viraj.dmabackend.auth.controller;

import com.viraj.dmabackend.auth.dto.PermissionResponse;
import com.viraj.dmabackend.auth.service.impl.PermissionServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "4. Permission Management", description = "Endpoints for retrieving system permissions")
@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class PermissionController {

    private final PermissionServiceImpl permissionService;

    @PreAuthorize("hasAuthority('user:read')")
    @GetMapping
    @Operation(summary = "Get All Permissions", description = "Retrieves a flat list of all available permissions in the system. Requires 'user:read' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Permissions retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public List<PermissionResponse> getAllPermissions() {
        return permissionService.getAllPermissions();
    }
}
