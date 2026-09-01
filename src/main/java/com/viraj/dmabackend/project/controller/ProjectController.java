package com.viraj.dmabackend.project.controller;

import com.viraj.dmabackend.project.dto.CreateProjectRequest;
import com.viraj.dmabackend.project.dto.ProjectResponse;
import com.viraj.dmabackend.project.dto.UpdateProjectRequest;
import com.viraj.dmabackend.project.enums.ProjectStatus;
import com.viraj.dmabackend.project.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "7. Project Management", description = "Endpoints for managing client projects")
@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class ProjectController {

    private final ProjectService projectService;

    @PreAuthorize("hasAuthority('project:create')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create Project", description = "Creates a new project for a client. Requires 'project:create' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Project created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public ProjectResponse createProject(
            @Valid @RequestBody CreateProjectRequest request) {
        return projectService.createProject(request);
    }

    @PreAuthorize("hasAuthority('project:read')")
    @GetMapping("/{projectId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get Project By Id", description = "Retrieves a specific project by ID. Requires 'project:read' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public ProjectResponse getProjectById(
            @Parameter(description = "Unique ID of the project", required = true) @PathVariable String projectId) {
        return projectService.getProjectById(projectId);
    }

    @PreAuthorize("hasAuthority('project:read')")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get All Projects", description = "Retrieves a paginated list of all projects. Requires 'project:read' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Projects retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public Page<ProjectResponse> getAllProjects(
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        return projectService.getAllProjects(pageable);
    }

    @PreAuthorize("hasAuthority('project:read')")
    @GetMapping("/client/{clientId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get Projects By Client", description = "Retrieves projects associated with a specific client. Requires 'project:read' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Projects retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Client not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public Page<ProjectResponse> getProjectsByClientId(
            @Parameter(description = "Unique ID of the client", required = true) @PathVariable String clientId,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        return projectService.getProjectsByClient(clientId, pageable);
    }

    @PreAuthorize("hasAuthority('project:read')")
    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Search Projects", description = "Searches for projects by keyword. Requires 'project:read' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search results returned successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public Page<ProjectResponse> searchProjects(
            @Parameter(description = "Keyword to search", required = true) @RequestParam String keyword,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        return projectService.searchProjects(keyword, pageable);
    }

    @PreAuthorize("hasAuthority('project:read')")
    @GetMapping("/status")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Filter Projects By Status", description = "Retrieves projects filtered by their status. Requires 'project:read' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Filtered projects returned successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public Page<ProjectResponse> filterByStatus(
            @Parameter(description = "Project status", required = true) @RequestParam ProjectStatus status,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        return projectService.getProjectsByStatus(status, pageable);
    }

    @PreAuthorize("hasAuthority('project:update')")
    @PutMapping("/{projectId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update Project", description = "Updates a project's details. Requires 'project:update' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public ProjectResponse updateProject(
            @Parameter(description = "Unique ID of the project", required = true) @PathVariable String projectId,
            @Valid @RequestBody UpdateProjectRequest request) {
        return projectService.updateProject(projectId, request);
    }

    @PreAuthorize("hasAuthority('project:delete')")
    @DeleteMapping("/{projectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete Project", description = "Deletes or archives a project. Requires 'project:delete' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Project successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public void deleteProject(
            @Parameter(description = "Unique ID of the project", required = true) @PathVariable String projectId) {
        projectService.deleteProject(projectId);
    }
}
