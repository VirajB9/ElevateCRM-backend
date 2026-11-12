package com.viraj.dmabackend.auth.controller;

import com.viraj.dmabackend.auth.dto.*;
import com.viraj.dmabackend.auth.service.RoleService;
import com.viraj.dmabackend.controller.BaseControllerWebMvcTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoleController.class)
class RoleControllerTest extends BaseControllerWebMvcTest {

    @MockBean
    private RoleService roleService;

    @Test
    @WithMockUser(authorities = "role:read")
    void getAllRoles_whenAuthorized_returnsOk() throws Exception {
        when(roleService.getAllRoles()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/roles"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "user:read")
    void getAllRoles_whenInsufficientAuthority_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/roles"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllRoles_whenUnauthenticated_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/roles"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "role:read")
    void getRoleById_whenAuthorized_returnsOk() throws Exception {
        when(roleService.getRoleById("r-1")).thenReturn(RoleResponse.builder().id("r-1").name("MANAGER").build());

        mockMvc.perform(get("/api/v1/roles/r-1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "role:update")
    void updateRole_whenAuthorized_returnsOk() throws Exception {
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setDescription("Senior Manager Role");

        when(roleService.updateRole(eq("r-1"), any())).thenReturn(RoleResponse.builder().id("r-1").name("SENIOR_MANAGER").build());

        mockMvc.perform(put("/api/v1/roles/r-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "role:read")
    void updateRole_whenInsufficientAuthority_returnsForbidden() throws Exception {
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setDescription("Senior Manager Role");

        mockMvc.perform(put("/api/v1/roles/r-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "role:update")
    void assignPermissions_whenAuthorized_returnsOk() throws Exception {
        AssignPermissionsRequest request = new AssignPermissionsRequest();
        request.setPermissionIds(List.of("perm-1"));

        when(roleService.assignPermissions(eq("r-1"), any())).thenReturn(RoleResponse.builder().id("r-1").build());

        mockMvc.perform(patch("/api/v1/roles/r-1/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "role:update")
    void removePermissions_whenAuthorized_returnsOk() throws Exception {
        RemovePermissionsRequest request = new RemovePermissionsRequest();
        request.setPermissionIds(List.of("perm-1"));

        when(roleService.removePermissions(eq("r-1"), any())).thenReturn(RoleResponse.builder().id("r-1").build());

        mockMvc.perform(delete("/api/v1/roles/r-1/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "role:read")
    void getUsersByRole_whenAuthorized_returnsOk() throws Exception {
        when(roleService.getUsersByRole(eq("r-1"), any())).thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/v1/roles/r-1/users"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "role:read")
    void getRoleById_withInvalidIdPattern_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/roles/invalid!role@id"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "role:read")
    void getUsersByRole_withInvalidIdPattern_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/roles/invalid!role@id/users"))
                .andExpect(status().isBadRequest());
    }
}
