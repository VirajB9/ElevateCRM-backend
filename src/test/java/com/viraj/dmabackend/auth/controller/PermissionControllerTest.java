package com.viraj.dmabackend.auth.controller;

import com.viraj.dmabackend.auth.dto.PermissionResponse;
import com.viraj.dmabackend.auth.service.PermissionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PermissionControllerTest {

    @Mock
    private PermissionService permissionService;

    @InjectMocks
    private PermissionController permissionController;

    @Test
    void getAllPermissions_DelegatesToService() {
        PermissionResponse perm = PermissionResponse.builder().id("p1").name("USER_READ").build();
        when(permissionService.getAllPermissions()).thenReturn(List.of(perm));

        List<PermissionResponse> result = permissionController.getAllPermissions();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("p1", result.get(0).getId());
        verify(permissionService).getAllPermissions();
    }
}
