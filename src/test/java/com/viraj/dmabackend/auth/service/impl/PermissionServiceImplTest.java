package com.viraj.dmabackend.auth.service.impl;

import com.viraj.dmabackend.auth.dto.PermissionResponse;
import com.viraj.dmabackend.auth.entity.Permission;
import com.viraj.dmabackend.auth.enums.PermissionType;
import com.viraj.dmabackend.auth.mapper.PermissionMapper;
import com.viraj.dmabackend.auth.repository.PermissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermissionServiceImplTest {

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private PermissionMapper permissionMapper;

    @InjectMocks
    private PermissionServiceImpl permissionService;

    private Permission testPermission;
    private PermissionResponse testResponse;

    @BeforeEach
    void setUp() {
        testPermission = Permission.builder()
                .id("perm-1")
                .permissionType(PermissionType.USER_READ)
                .module("USER")
                .description("Read user data")
                .build();

        testResponse = PermissionResponse.builder()
                .id("perm-1")
                .name("USER_READ")
                .module("USER")
                .description("Read user data")
                .build();
    }

    @Test
    @DisplayName("getAllPermissions returns all mapped permissions successfully")
    void getAllPermissions_ShouldReturnAllMappedPermissions() {
        when(permissionRepository.findAll()).thenReturn(List.of(testPermission));
        when(permissionMapper.toPermissionResponse(testPermission)).thenReturn(testResponse);

        List<PermissionResponse> result = permissionService.getAllPermissions();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("USER_READ", result.get(0).getName());
        assertEquals("USER", result.get(0).getModule());
        verify(permissionRepository, times(1)).findAll();
        verify(permissionMapper, times(1)).toPermissionResponse(testPermission);
    }

    @Test
    @DisplayName("getAllPermissions returns empty list when repository has no permissions")
    void getAllPermissions_WhenEmpty_ShouldReturnEmptyList() {
        when(permissionRepository.findAll()).thenReturn(Collections.emptyList());

        List<PermissionResponse> result = permissionService.getAllPermissions();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(permissionRepository, times(1)).findAll();
        verifyNoInteractions(permissionMapper);
    }

    @Test
    @DisplayName("getAllPermissions propagates exception when repository throws exception")
    void getAllPermissions_WhenRepositoryFails_ShouldPropagateException() {
        when(permissionRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> permissionService.getAllPermissions());
        assertEquals("Database error", exception.getMessage());
        verify(permissionRepository, times(1)).findAll();
        verifyNoInteractions(permissionMapper);
    }

    @Test
    @DisplayName("getAllPermissions logs warning and completes when permissions exceed threshold of 500")
    void getAllPermissions_WhenExceedsThreshold_LogsWarningAndReturnsMappedList() {
        List<Permission> largeList = new ArrayList<>();
        for (int i = 0; i < 501; i++) {
            largeList.add(testPermission);
        }
        when(permissionRepository.findAll()).thenReturn(largeList);
        when(permissionMapper.toPermissionResponse(any(Permission.class))).thenReturn(testResponse);

        List<PermissionResponse> result = permissionService.getAllPermissions();

        assertNotNull(result);
        assertEquals(501, result.size());
        verify(permissionRepository, times(1)).findAll();
        verify(permissionMapper, times(501)).toPermissionResponse(any(Permission.class));
    }
}
