package com.viraj.dmabackend.auth.controller;

import com.viraj.dmabackend.auth.dto.PermissionResponse;
import com.viraj.dmabackend.auth.service.PermissionService;
import com.viraj.dmabackend.controller.BaseControllerWebMvcTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PermissionController.class)
class PermissionControllerTest extends BaseControllerWebMvcTest {

    @MockBean
    private PermissionService permissionService;

    @Test
    @WithMockUser(authorities = "role:read")
    void getAllPermissions_whenAuthorized_returnsOk() throws Exception {
        when(permissionService.getAllPermissions()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/permissions"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "lead:read")
    void getAllPermissions_whenInsufficientAuthority_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/permissions"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllPermissions_whenUnauthenticated_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/permissions"))
                .andExpect(status().isUnauthorized());
    }
}
