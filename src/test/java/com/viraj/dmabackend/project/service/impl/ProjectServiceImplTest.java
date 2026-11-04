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
}
