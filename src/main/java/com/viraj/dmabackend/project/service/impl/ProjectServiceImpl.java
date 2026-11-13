package com.viraj.dmabackend.project.service.impl;

import com.viraj.dmabackend.common.util.PaginationUtils;
import com.viraj.dmabackend.exception.BadRequestException;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final ProjectValidator projectValidator;

    @Transactional
    @Override
    public ProjectResponse createProject(CreateProjectRequest request) {

        projectValidator.validateClientActiveForProject(request.getClientId());
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

        if (status == ProjectStatus.ARCHIVED) {
            throw new BadRequestException("Querying archived projects is not permitted");
        }

        return projectRepository.findByStatus(status, PaginationUtils.clampPageable(pageable))
                .map(projectMapper::toProjectResponse);
    }

    @Override
    public Page<ProjectResponse> getProjectsByClient(String clientId, Pageable pageable) {

        String trimmedClientId = clientId != null ? clientId.trim() : null;
        projectValidator.validateClientExists(trimmedClientId);

        return projectRepository.findByClientIdAndStatusNot(trimmedClientId, ProjectStatus.ARCHIVED, PaginationUtils.clampPageable(pageable))
                .map(projectMapper::toProjectResponse);
    }

    @Override
    public Page<ProjectResponse> getAllProjects(Pageable pageable) {

        return projectRepository.findByStatusNot(ProjectStatus.ARCHIVED, PaginationUtils.clampPageable(pageable))
                .map(projectMapper::toProjectResponse);
    }

    @Override
    public Page<ProjectResponse> searchProjects(String keyword, Pageable pageable) {

        Page<Project> projects = projectRepository.searchProjects(keyword, PaginationUtils.clampPageable(pageable));
        return projects.map(projectMapper::toProjectResponse);
    }

    @Transactional
    @Override
    public ProjectResponse updateProject(String projectId, UpdateProjectRequest request) {

        Project project = findProjectById(projectId);

        if (request.getVersion() != null && !request.getVersion().equals(project.getVersion())) {
            throw new org.springframework.dao.OptimisticLockingFailureException(
                    "Project has been modified by another operation. Please refresh and try again."
            );
        }

        projectValidator.validateProjectEditable(project.getStatus());
        projectValidator.validateProjectStatusForUpdate(request.getStatus());
        projectValidator.validateProjectStatusTransition(project.getStatus(), request.getStatus());
        if (request.getStatus() == ProjectStatus.ACTIVE) {
            projectValidator.validateClientActiveForProject(project.getClientId());
            projectValidator.validateActiveProjectDates(request.getStatus(), request.getStartDate(), request.getEndDate());
        }
        if (request.getStatus() == ProjectStatus.COMPLETED) {
            projectValidator.validateProjectCompletion(request.getEndDate());
            projectValidator.validateProjectCompletionGuards(projectId);
        }
        projectValidator.validateDuplicateProjectForUpdate(
                request.getProjectName() != null ? request.getProjectName().trim() : null,
                project.getClientId(),
                projectId);
        projectValidator.validateProjectDates(request.getStartDate(), request.getEndDate());

        updateProjectFields(project, request);

        Project updatedProject = projectRepository.save(project);

        return projectMapper.toProjectResponse(updatedProject);
    }

    @Transactional
    @Override
    public void deleteProject(String projectId) {

        Project project = findProjectById(projectId);
        projectValidator.validateProjectDeletable(project);
        project.setStatus(ProjectStatus.ARCHIVED);
        project.setDeleted(true);
        projectRepository.save(project);
    }


    // =========================
    // Helper Methods
    // =========================
    private Project findProjectById(String projectId) {
        String trimmedProjectId = projectId != null ? projectId.trim() : null;
        return projectRepository.findByIdAndStatusNot(trimmedProjectId, ProjectStatus.ARCHIVED)
                .orElseThrow(() ->
                        new ProjectNotFoundException(projectId));
    }

    private Project buildProject(CreateProjectRequest request) {

        return Project.builder()
                .clientId(normalizeTrim(request.getClientId()))
                .projectName(normalizeTrim(request.getProjectName()))
                .description(normalizeTrim(request.getDescription()))
                .priority(request.getPriority())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .budget(normalizeBudget(request.getBudget()))
                .notes(normalizeTrim(request.getNotes()))
                .build();
    }

    private void updateProjectFields(Project project, UpdateProjectRequest request) {

        project.setProjectName(normalizeTrim(request.getProjectName()));
        project.setDescription(normalizeTrim(request.getDescription()));
        project.setPriority(request.getPriority());
        project.setStatus(request.getStatus());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setBudget(normalizeBudget(request.getBudget()));
        project.setNotes(normalizeTrim(request.getNotes()));
    }

    private BigDecimal normalizeBudget(BigDecimal budget) {
        return budget != null ? budget.setScale(2, RoundingMode.HALF_UP) : null;
    }

    private String normalizeTrim(String value) {
        return value != null ? value.trim() : null;
    }
}
