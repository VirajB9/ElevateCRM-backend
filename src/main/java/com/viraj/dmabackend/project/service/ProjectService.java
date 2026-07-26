package com.viraj.dmabackend.project.service;

import com.viraj.dmabackend.project.dto.CreateProjectRequest;
import com.viraj.dmabackend.project.dto.ProjectResponse;
import com.viraj.dmabackend.project.dto.UpdateProjectRequest;
import com.viraj.dmabackend.project.enums.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectService {

    ProjectResponse createProject(CreateProjectRequest request);

    ProjectResponse getProjectById(String projectId);

    Page<ProjectResponse> getAllProjects(Pageable pageable);

    Page<ProjectResponse> searchProjects(String keyword, Pageable pageable);

    Page<ProjectResponse> getProjectsByStatus(ProjectStatus status, Pageable pageable);

    Page<ProjectResponse> getProjectsByClient(String clientId, Pageable pageable);

    ProjectResponse updateProject(String projectId, UpdateProjectRequest request);

    void deleteProject(String projectId);
}
