package com.viraj.dmabackend.menu.controller;

import com.viraj.dmabackend.controller.BaseControllerWebMvcTest;
import com.viraj.dmabackend.menu.dto.CreateMenuRequest;
import com.viraj.dmabackend.menu.dto.MenuResponse;
import com.viraj.dmabackend.menu.dto.UpdateMenuRequest;
import com.viraj.dmabackend.menu.service.MenuService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MenuController.class)
class MenuControllerTest extends BaseControllerWebMvcTest {

    @MockBean
    private MenuService menuService;

    @Test
    @WithMockUser(authorities = "menu:create")
    void createMenu_whenAuthorized_returnsCreated() throws Exception {
        CreateMenuRequest request = new CreateMenuRequest();
        request.setTitle("Dashboard");
        request.setPath("/dashboard");
        request.setIcon("dashboard-icon");
        request.setOrderIndex(1);

        when(menuService.createMenu(any())).thenReturn(MenuResponse.builder().id("m-1").build());

        mockMvc.perform(post("/api/v1/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(authorities = "menu:read")
    void createMenu_whenInsufficientAuthority_returnsForbidden() throws Exception {
        CreateMenuRequest request = new CreateMenuRequest();
        request.setTitle("Dashboard");
        request.setPath("/dashboard");
        request.setIcon("dashboard-icon");
        request.setOrderIndex(1);

        mockMvc.perform(post("/api/v1/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createMenu_whenUnauthenticated_returnsUnauthorized() throws Exception {
        CreateMenuRequest request = new CreateMenuRequest();
        request.setTitle("Dashboard");
        request.setPath("/dashboard");

        mockMvc.perform(post("/api/v1/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "menu:read")
    void getMenuById_whenAuthorized_returnsOk() throws Exception {
        when(menuService.getMenuById("m-1")).thenReturn(MenuResponse.builder().id("m-1").build());

        mockMvc.perform(get("/api/v1/menus/m-1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "menu:read")
    void getAllMenus_whenAuthorized_returnsOk() throws Exception {
        when(menuService.getAllMenus()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/menus"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "menu:update")
    void updateMenu_whenAuthorized_returnsOk() throws Exception {
        UpdateMenuRequest request = new UpdateMenuRequest();
        request.setTitle("New Dashboard");
        request.setPath("/new-dashboard");
        request.setIcon("new-icon");
        request.setOrderIndex(1);
        request.setActive(true);
        request.setVersion(1L);

        when(menuService.updateMenu(eq("m-1"), any())).thenReturn(MenuResponse.builder().id("m-1").build());

        mockMvc.perform(put("/api/v1/menus/m-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "menu:delete")
    void deactivateMenu_whenAuthorized_returnsOk() throws Exception {
        doNothing().when(menuService).deactivateMenu("m-1");

        mockMvc.perform(patch("/api/v1/menus/m-1/deactivate"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "menu:read")
    void deactivateMenu_whenInsufficientAuthority_returnsForbidden() throws Exception {
        mockMvc.perform(patch("/api/v1/menus/m-1/deactivate"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "menu:read")
    void getMenuTree_whenAuthorized_returnsOk() throws Exception {
        when(menuService.getMenuTree()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/menus/tree"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "menu:read")
    void getMenuById_withInvalidIdPattern_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/menus/invalid!menu@id"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "menu:delete")
    void deactivateMenu_withInvalidIdPattern_returnsBadRequest() throws Exception {
        mockMvc.perform(patch("/api/v1/menus/invalid!menu@id/deactivate"))
                .andExpect(status().isBadRequest());
    }
}
