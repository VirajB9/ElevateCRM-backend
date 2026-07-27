package com.viraj.dmabackend.project.controller;

import com.viraj.dmabackend.project.dto.CreateProjectRequest;
import com.viraj.dmabackend.project.dto.ProjectResponse;
import com.viraj.dmabackend.project.dto.UpdateProjectRequest;
import com.viraj.dmabackend.project.enums.ProjectStatus;
import com.viraj.dmabackend.project.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "6. Project Management")
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class ProjectController {

    private final ProjectService projectService;


    @PreAuthorize("hasAuthority('project:create')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create project")
    public ProjectResponse createProject(
            @Valid @RequestBody CreateProjectRequest request) {

        return projectService.createProject(request);
    }

    @PreAuthorize("hasAuthority('project:read')")
    @GetMapping("/{projectId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get Project By Id")
    public ProjectResponse getProjectById(
            @PathVariable String projectId) {

        return projectService.getProjectById(projectId);
    }

    @PreAuthorize("hasAuthority('project:read')")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get All Projects")
    public Page<ProjectResponse> getAllProjects(
            Pageable pageable) {

        return projectService.getAllProjects(pageable);
    }

    @PreAuthorize("hasAuthority('project:read')")
    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Search Projects")
    public Page<ProjectResponse> searchProjects(
            @RequestParam String keyword, Pageable pageable) {

        return projectService.searchProjects(keyword, pageable);
    }

    @PreAuthorize("hasAuthority('project:read')")
    @GetMapping("/status")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Filter Projects By Status")
    public Page<ProjectResponse> filterProjectsByStatus(
            @RequestParam ProjectStatus status, Pageable pageable) {

        return projectService.getProjectsByStatus(status, pageable);
    }

    @PreAuthorize("hasAuthority('project:read')")
    @GetMapping("/client/{clientId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get Projects By Client")
    public Page<ProjectResponse> getProjectsByClient(
            @PathVariable String clientId, Pageable pageable) {

        return projectService.getProjectsByClient(clientId, pageable);
    }

    @PreAuthorize("hasAuthority('project:update')")
    @PutMapping("/{projectId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update Project")
    public ProjectResponse updateProject(
            @PathVariable String projectId,
            @Valid @RequestBody UpdateProjectRequest request) {

        return projectService.updateProject(projectId, request);
    }

    @PreAuthorize("hasAuthority('project:delete')")
    @DeleteMapping("/{projectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete Project")
    public void deleteProject(
            @PathVariable String projectId) {

        projectService.deleteProject(projectId);
    }
}