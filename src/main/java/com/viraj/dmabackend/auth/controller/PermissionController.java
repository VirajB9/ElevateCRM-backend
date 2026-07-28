package com.viraj.dmabackend.auth.controller;

import com.viraj.dmabackend.auth.dto.PermissionResponse;
import com.viraj.dmabackend.auth.service.impl.PermissionServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "4. Permission Management")
@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class PermissionController {

    private final PermissionServiceImpl permissionService;

    @PreAuthorize("hasAuthority('user:read')")
    @GetMapping
    @Operation(summary = "Get All Permissions")
    public List<PermissionResponse> getAllPermissions() {

        return permissionService.getAllPermissions();
    }
}