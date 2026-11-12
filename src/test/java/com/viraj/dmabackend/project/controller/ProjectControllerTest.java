package com.viraj.dmabackend.project.controller;

import com.viraj.dmabackend.controller.BaseControllerWebMvcTest;
import com.viraj.dmabackend.project.dto.CreateProjectRequest;
import com.viraj.dmabackend.project.dto.ProjectResponse;
import com.viraj.dmabackend.project.dto.UpdateProjectRequest;
import com.viraj.dmabackend.project.enums.ProjectPriority;
import com.viraj.dmabackend.project.enums.ProjectStatus;
import com.viraj.dmabackend.project.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
class ProjectControllerTest extends BaseControllerWebMvcTest {

    @MockBean
    private ProjectService projectService;

    @Test
    @WithMockUser(authorities = "project:create")
    void createProject_whenAuthorized_returnsCreated() throws Exception {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setProjectName("SEO Campaign");
        request.setClientId("client-1");
        request.setPriority(ProjectPriority.HIGH);
        request.setStartDate(LocalDate.now());

        when(projectService.createProject(any())).thenReturn(ProjectResponse.builder().id("p-1").build());

        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(authorities = "project:read")
    void createProject_whenInsufficientAuthority_returnsForbidden() throws Exception {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setProjectName("SEO Campaign");
        request.setClientId("client-1");
        request.setPriority(ProjectPriority.HIGH);
        request.setStartDate(LocalDate.now());

        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createProject_whenUnauthenticated_returnsUnauthorized() throws Exception {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setProjectName("SEO Campaign");
        request.setClientId("client-1");

        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "project:read")
    void getProjectById_whenAuthorized_returnsOk() throws Exception {
        when(projectService.getProjectById("p-1")).thenReturn(ProjectResponse.builder().id("p-1").build());

        mockMvc.perform(get("/api/v1/projects/p-1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "project:read")
    void getAllProjects_whenAuthorized_returnsOk() throws Exception {
        when(projectService.getAllProjects(any())).thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/v1/projects"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "project:read")
    void getProjectsByClientId_whenAuthorized_returnsOk() throws Exception {
        when(projectService.getProjectsByClient(eq("client-1"), any())).thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/v1/projects/client/client-1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "project:read")
    void searchProjects_whenAuthorized_returnsOk() throws Exception {
        when(projectService.searchProjects(eq("seo"), any())).thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/v1/projects/search")
                        .param("keyword", "seo"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "project:read")
    void filterByStatus_whenAuthorized_returnsOk() throws Exception {
        when(projectService.getProjectsByStatus(eq(ProjectStatus.ACTIVE), any())).thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/v1/projects/status")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "project:update")
    void updateProject_whenAuthorized_returnsOk() throws Exception {
        UpdateProjectRequest request = new UpdateProjectRequest();
        request.setProjectName("SEO Optimization 2026");
        request.setPriority(ProjectPriority.HIGH);
        request.setStatus(ProjectStatus.ACTIVE);
        request.setStartDate(LocalDate.now());

        when(projectService.updateProject(eq("p-1"), any())).thenReturn(ProjectResponse.builder().id("p-1").build());

        mockMvc.perform(put("/api/v1/projects/p-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "project:delete")
    void deleteProject_whenAuthorized_returnsNoContent() throws Exception {
        doNothing().when(projectService).deleteProject("p-1");

        mockMvc.perform(delete("/api/v1/projects/p-1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = "project:read")
    void deleteProject_whenInsufficientAuthority_returnsForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/projects/p-1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "project:read")
    void getProjectById_withInvalidIdPattern_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/projects/invalid!proj@id"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "project:read")
    void searchProjects_withOversizedKeyword_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/projects/search")
                        .param("keyword", "p".repeat(101)))
                .andExpect(status().isBadRequest());
    }
}
