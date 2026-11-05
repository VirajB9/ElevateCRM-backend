package com.viraj.dmabackend.project.mapper;

import com.viraj.dmabackend.project.dto.ProjectResponse;
import com.viraj.dmabackend.project.entity.Project;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectMapperTest {

    private final ProjectMapper projectMapper = new ProjectMapper();

    @Test
    void toProjectResponse_whenProjectIsNull_returnsNullWithoutNPE() {
        ProjectResponse response = projectMapper.toProjectResponse(null);
        assertThat(response).isNull();
    }

    @Test
    void toProjectResponse_whenProjectIsNotNull_mapsAllFields() {
        Project project = Project.builder()
                .id("proj-1")
                .projectName("CRM")
                .clientId("client-1")
                .build();

        ProjectResponse response = projectMapper.toProjectResponse(project);
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("proj-1");
        assertThat(response.getProjectName()).isEqualTo("CRM");
        assertThat(response.getClientId()).isEqualTo("client-1");
    }
}
