package com.viraj.dmabackend.project.service.impl;

import com.viraj.dmabackend.client.exception.ClientNotFoundException;
import com.viraj.dmabackend.client.repository.ClientRepository;
import com.viraj.dmabackend.project.dto.CreateProjectRequest;
import com.viraj.dmabackend.project.dto.ProjectResponse;
import com.viraj.dmabackend.project.dto.UpdateProjectRequest;
import com.viraj.dmabackend.project.entity.Project;
import com.viraj.dmabackend.project.enums.ProjectStatus;
import com.viraj.dmabackend.project.exception.DuplicateProjectException;
import com.viraj.dmabackend.project.exception.InvalidProjectDateException;
import com.viraj.dmabackend.project.exception.ProjectNotFoundException;
import com.viraj.dmabackend.project.repository.ProjectRepository;
import com.viraj.dmabackend.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;

    private final ClientRepository clientRepository;

    @Override
    public ProjectResponse createProject(CreateProjectRequest request) {

        validateClientExists(request.getClientId());

        validateDuplicateProject(request.getProjectName(), request.getClientId());

        validateProjectDates(request.getStartDate(), request.getEndDate());

        Project project = buildProject(request);

        Project savedProject = projectRepository.save(project);

        return mapToProjectResponse(savedProject);
    }

    @Override
    public ProjectResponse getProjectById(String projectId) {

        Project project = findProjectById(projectId);

        return mapToProjectResponse(project);
    }

    @Override
    public Page<ProjectResponse> getProjectsByStatus(ProjectStatus status, Pageable pageable) {
        return projectRepository.findByStatus(status, pageable)
                .map(this::mapToProjectResponse);
    }

    @Override
    public Page<ProjectResponse> getProjectsByClient(String clientId, Pageable pageable) {

        validateClientExists(clientId);
        return projectRepository.findByClientId(clientId, pageable)
                .map(this::mapToProjectResponse);

    }

    @Override
    public Page<ProjectResponse> getAllProjects(Pageable pageable) {

        return projectRepository.findAll(pageable)
                .map(this::mapToProjectResponse);
    }

    @Override
    public Page<ProjectResponse> searchProjects(String keyword, Pageable pageable) {
        return projectRepository
                .findByProjectNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                        keyword,
                        keyword,
                        pageable)
                .map(this::mapToProjectResponse);
    }

    @Override
    public ProjectResponse updateProject(String projectId, UpdateProjectRequest request) {

        Project project = findProjectById(projectId);

        validateDuplicateProjectForUpdate(request.getProjectName(), project.getClientId(), projectId);

        validateProjectDates(request.getStartDate(), request.getEndDate());

        updateProjectFields(project, request);

        Project updatedProject = projectRepository.save(project);

        return mapToProjectResponse(updatedProject);
    }

    @Override
    public void deleteProject(String projectId) {

        Project project = findProjectById(projectId);

        projectRepository.delete(project);
    }


    // =========================
    // Helper Methods
    // =========================
    private Project findProjectById(String projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new ProjectNotFoundException(projectId));
    }

    private void validateClientExists(String clientId) {

        if (!clientRepository.existsById(clientId)) {
            throw new ClientNotFoundException(clientId);
        }
    }

    private void validateDuplicateProject(String projectName, String clientId) {

        if (projectRepository.existsByProjectNameAndClientId(projectName, clientId)) {
            throw new DuplicateProjectException(projectName, clientId);
        }
    }

    private void validateProjectDates(LocalDate startDate, LocalDate endDate) {

        if (startDate != null
                && endDate != null
                && endDate.isBefore(startDate)) {
            throw new InvalidProjectDateException();
        }
    }

    private void validateDuplicateProjectForUpdate(String projectName, String clientId, String projectId) {

        if (projectRepository.existsByProjectNameAndClientIdAndIdNot(projectName, clientId, projectId)) {

            throw new DuplicateProjectException(projectName, clientId);
        }
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

    private ProjectResponse mapToProjectResponse(Project project) {

        return ProjectResponse.builder()
                .id(project.getId())
                .clientId(project.getClientId())
                .projectName(project.getProjectName())
                .description(project.getDescription())
                .status(project.getStatus())
                .priority(project.getPriority())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .budget(project.getBudget())
                .notes(project.getNotes())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .createdBy(project.getCreatedBy())
                .updatedBy(project.getUpdatedBy())
                .build();
    }
}
