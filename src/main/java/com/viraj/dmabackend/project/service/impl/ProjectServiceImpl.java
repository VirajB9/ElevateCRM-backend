package com.viraj.dmabackend.project.service.impl;

import com.viraj.dmabackend.project.dto.CreateProjectRequest;
import com.viraj.dmabackend.project.dto.ProjectResponse;
import com.viraj.dmabackend.project.dto.UpdateProjectRequest;
import com.viraj.dmabackend.project.entity.Project;
import com.viraj.dmabackend.project.enums.ProjectStatus;
import com.viraj.dmabackend.project.exception.ProjectNotFoundException;
import com.viraj.dmabackend.project.mapper.ProjectMapper;
import com.viraj.dmabackend.project.repository.ProjectRepository;
import com.viraj.dmabackend.project.service.ProjectService;
import com.viraj.dmabackend.project.validator.ProjectValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final ProjectValidator projectValidator;

    @Override
    public ProjectResponse createProject(CreateProjectRequest request) {

        projectValidator.validateClientExists(request.getClientId());
        projectValidator.validateDuplicateProject(request.getProjectName(), request.getClientId());
        projectValidator.validateProjectDates(request.getStartDate(), request.getEndDate());

        Project project = buildProject(request);
        Project savedProject = projectRepository.save(project);

        return projectMapper.toProjectResponse(savedProject);
    }

    @Override
    public ProjectResponse getProjectById(String projectId) {

        Project project = findProjectById(projectId);
        return projectMapper.toProjectResponse(project);
    }

    @Override
    public Page<ProjectResponse> getProjectsByStatus(ProjectStatus status, Pageable pageable) {

        return projectRepository.findByStatus(status, pageable)
                .map(projectMapper::toProjectResponse);
    }

    @Override
    public Page<ProjectResponse> getProjectsByClient(String clientId, Pageable pageable) {

        projectValidator.validateClientExists(clientId);
        return projectRepository.findByClientId(clientId, pageable)
                .map(projectMapper::toProjectResponse);
    }

    @Override
    public Page<ProjectResponse> getAllProjects(Pageable pageable) {

        return projectRepository.findAll(pageable)
                .map(projectMapper::toProjectResponse);
    }

    @Override
    public Page<ProjectResponse> searchProjects(String keyword, Pageable pageable) {

        Page<Project> projects = projectRepository.searchProjects(keyword, pageable);
        return projects.map(projectMapper::toProjectResponse);
    }

    @Override
    public ProjectResponse updateProject(String projectId, UpdateProjectRequest request) {

        Project project = findProjectById(projectId);

        projectValidator.validateDuplicateProjectForUpdate(request.getProjectName(), project.getClientId(), projectId);
        projectValidator.validateProjectDates(request.getStartDate(), request.getEndDate());

        updateProjectFields(project, request);

        Project updatedProject = projectRepository.save(project);

        return projectMapper.toProjectResponse(updatedProject);
    }

    @Override
    public void deleteProject(String projectId) {

        Project project = findProjectById(projectId);
        project.setStatus(ProjectStatus.ARCHIVED);
        projectRepository.save(project);
    }


    // =========================
    // Helper Methods
    // =========================
    private Project findProjectById(String projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new ProjectNotFoundException(projectId));
    }

    private Project buildProject(CreateProjectRequest request) {

        return Project.builder()
                .clientId(request.getClientId())
                .projectName(request.getProjectName())
                .description(request.getDescription())
                .priority(request.getPriority())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .budget(request.getBudget())
                .notes(request.getNotes())
                .build();
    }

    private void updateProjectFields(Project project, UpdateProjectRequest request) {

        project.setProjectName(request.getProjectName());
        project.setDescription(request.getDescription());
        project.setPriority(request.getPriority());
        project.setStatus(request.getStatus());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setBudget(request.getBudget());
        project.setNotes(request.getNotes());
    }
}
