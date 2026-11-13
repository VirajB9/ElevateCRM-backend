package com.viraj.dmabackend.project.service.impl;

import com.viraj.dmabackend.exception.BadRequestException;
import com.viraj.dmabackend.project.dto.UpdateProjectRequest;
import com.viraj.dmabackend.project.entity.Project;
import com.viraj.dmabackend.project.enums.ProjectPriority;
import com.viraj.dmabackend.project.enums.ProjectStatus;
import com.viraj.dmabackend.project.mapper.ProjectMapper;
import com.viraj.dmabackend.project.repository.ProjectRepository;
import com.viraj.dmabackend.project.validator.ProjectValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private ProjectValidator projectValidator;

    @InjectMocks
    private ProjectServiceImpl projectService;

    @Test
    void updateProject_ThrowsWhenArchivedStatusRequested() {
        Project project = new Project();
        project.setId("proj-1");
        project.setClientId("client-1");
        project.setProjectName("Test Project");
        project.setStatus(ProjectStatus.ACTIVE);

        when(projectRepository.findByIdAndStatusNot("proj-1", ProjectStatus.ARCHIVED))
                .thenReturn(Optional.of(project));
        doThrow(new BadRequestException("Projects cannot be archived via update. Please use the delete project endpoint."))
                .when(projectValidator).validateProjectStatusForUpdate(ProjectStatus.ARCHIVED);

        UpdateProjectRequest req = new UpdateProjectRequest();
        req.setProjectName("Test Project");
        req.setPriority(ProjectPriority.MEDIUM);
        req.setStatus(ProjectStatus.ARCHIVED);
        req.setStartDate(LocalDate.now());

        assertThrows(BadRequestException.class, () -> projectService.updateProject("proj-1", req));
        verify(projectRepository, never()).save(any());
    }

    @Test
    void updateProject_whenCompleting_validatesProjectCompletionGuards() {
        Project project = new Project();
        project.setId("proj-1");
        project.setClientId("client-1");
        project.setProjectName("Test Project");
        project.setStatus(ProjectStatus.ACTIVE);

        when(projectRepository.findByIdAndStatusNot("proj-1", ProjectStatus.ARCHIVED))
                .thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenAnswer(i -> i.getArgument(0));
        when(projectMapper.toProjectResponse(any())).thenReturn(com.viraj.dmabackend.project.dto.ProjectResponse.builder().build());

        UpdateProjectRequest req = new UpdateProjectRequest();
        req.setProjectName("Test Project");
        req.setPriority(ProjectPriority.MEDIUM);
        req.setStatus(ProjectStatus.COMPLETED);
        req.setStartDate(LocalDate.now());
        req.setEndDate(LocalDate.now());

        projectService.updateProject("proj-1", req);

        verify(projectValidator).validateProjectCompletion(req.getEndDate());
        verify(projectValidator).validateProjectCompletionGuards("proj-1");
    }

    @Test
    void updateProject_ThrowsWhenProjectIsCancelled() {
        Project project = new Project();
        project.setId("proj-1");
        project.setClientId("client-1");
        project.setProjectName("Test Project");
        project.setStatus(ProjectStatus.CANCELLED);

        when(projectRepository.findByIdAndStatusNot("proj-1", ProjectStatus.ARCHIVED))
                .thenReturn(Optional.of(project));
        doThrow(new BadRequestException("Cancelled projects cannot be edited."))
                .when(projectValidator).validateProjectEditable(ProjectStatus.CANCELLED);

        UpdateProjectRequest req = new UpdateProjectRequest();
        req.setProjectName("Test Project");
        req.setPriority(ProjectPriority.MEDIUM);
        req.setStatus(ProjectStatus.ACTIVE);
        req.setStartDate(LocalDate.now());

        assertThrows(BadRequestException.class, () -> projectService.updateProject("proj-1", req));
        verify(projectRepository, never()).save(any());
    }

    @Test
    void createProject_NormalizesTrimProjectName() {
        com.viraj.dmabackend.project.dto.CreateProjectRequest req = new com.viraj.dmabackend.project.dto.CreateProjectRequest();
        req.setClientId(" client-1 ");
        req.setProjectName("   Trimmed Project Name   ");
        req.setDescription("   Some description   ");
        req.setPriority(ProjectPriority.HIGH);
        req.setStartDate(LocalDate.now());
        req.setNotes("   Project notes   ");

        org.mockito.ArgumentCaptor<Project> captor = org.mockito.ArgumentCaptor.forClass(Project.class);
        when(projectRepository.save(captor.capture())).thenAnswer(i -> i.getArgument(0));
        when(projectMapper.toProjectResponse(any())).thenReturn(com.viraj.dmabackend.project.dto.ProjectResponse.builder().build());

        projectService.createProject(req);

        Project saved = captor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals("client-1", saved.getClientId());
        org.junit.jupiter.api.Assertions.assertEquals("Trimmed Project Name", saved.getProjectName());
        org.junit.jupiter.api.Assertions.assertEquals("Some description", saved.getDescription());
        org.junit.jupiter.api.Assertions.assertEquals("Project notes", saved.getNotes());
    }

    @Test
    void createProject_NormalizesBudgetToTwoDecimalPlaces() {
        com.viraj.dmabackend.project.dto.CreateProjectRequest req = new com.viraj.dmabackend.project.dto.CreateProjectRequest();
        req.setClientId("client-1");
        req.setProjectName("CRM");
        req.setPriority(ProjectPriority.HIGH);
        req.setStartDate(LocalDate.now());
        req.setBudget(new java.math.BigDecimal("1500.456"));

        org.mockito.ArgumentCaptor<Project> captor = org.mockito.ArgumentCaptor.forClass(Project.class);
        when(projectRepository.save(captor.capture())).thenAnswer(i -> i.getArgument(0));
        when(projectMapper.toProjectResponse(any())).thenReturn(com.viraj.dmabackend.project.dto.ProjectResponse.builder().build());

        projectService.createProject(req);

        Project saved = captor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals(new java.math.BigDecimal("1500.46"), saved.getBudget());
    }

    @Test
    void getProjectsByClient_TrimsClientId() {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        Project project = Project.builder().id("proj-1").clientId("client-123").build();
        when(projectRepository.findByClientIdAndStatusNot("client-123", ProjectStatus.ARCHIVED, pageable))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(java.util.List.of(project)));
        when(projectMapper.toProjectResponse(project))
                .thenReturn(com.viraj.dmabackend.project.dto.ProjectResponse.builder().id("proj-1").build());

        org.springframework.data.domain.Page<com.viraj.dmabackend.project.dto.ProjectResponse> result =
                projectService.getProjectsByClient("   client-123   ", pageable);

        org.junit.jupiter.api.Assertions.assertNotNull(result);
        verify(projectValidator).validateClientExists("client-123");
        verify(projectRepository).findByClientIdAndStatusNot("client-123", ProjectStatus.ARCHIVED, pageable);
    }

    @Test
    void getProjectById_TrimsProjectId() {
        Project project = Project.builder().id("proj-1").projectName("CRM").build();
        when(projectRepository.findByIdAndStatusNot("proj-1", ProjectStatus.ARCHIVED))
                .thenReturn(Optional.of(project));
        when(projectMapper.toProjectResponse(project))
                .thenReturn(com.viraj.dmabackend.project.dto.ProjectResponse.builder().id("proj-1").build());

        com.viraj.dmabackend.project.dto.ProjectResponse response = projectService.getProjectById("   proj-1   ");

        org.junit.jupiter.api.Assertions.assertNotNull(response);
        verify(projectRepository).findByIdAndStatusNot("proj-1", ProjectStatus.ARCHIVED);
    }

    @Test
    void deleteProject_ValidatesAndArchivesProject() {
        Project project = Project.builder().id("proj-1").status(ProjectStatus.COMPLETED).build();
        when(projectRepository.findByIdAndStatusNot("proj-1", ProjectStatus.ARCHIVED))
                .thenReturn(Optional.of(project));

        projectService.deleteProject("proj-1");

        verify(projectValidator).validateProjectDeletable(project);
        org.junit.jupiter.api.Assertions.assertEquals(ProjectStatus.ARCHIVED, project.getStatus());
        org.junit.jupiter.api.Assertions.assertTrue(project.isDeleted());
        verify(projectRepository).save(project);
    }

    @Test
    void deleteProject_ThrowsWhenProjectValidationFails() {
        Project project = Project.builder().id("proj-1").status(ProjectStatus.ACTIVE).build();
        when(projectRepository.findByIdAndStatusNot("proj-1", ProjectStatus.ARCHIVED))
                .thenReturn(Optional.of(project));
        doThrow(new BadRequestException("Only completed or cancelled projects can be archived"))
                .when(projectValidator).validateProjectDeletable(project);

        assertThrows(BadRequestException.class, () -> projectService.deleteProject("proj-1"));
        verify(projectRepository, never()).save(any());
    }

    @Test
    void updateProject_ThrowsOptimisticLockingFailure_WhenVersionMismatch() {
        Project project = Project.builder().id("proj-1").status(ProjectStatus.ACTIVE).build();
        project.setVersion(2L);
        when(projectRepository.findByIdAndStatusNot("proj-1", ProjectStatus.ARCHIVED))
                .thenReturn(Optional.of(project));

        UpdateProjectRequest request = new UpdateProjectRequest();
        request.setVersion(1L);

        assertThrows(org.springframework.dao.OptimisticLockingFailureException.class,
                () -> projectService.updateProject("proj-1", request));
    }
}
