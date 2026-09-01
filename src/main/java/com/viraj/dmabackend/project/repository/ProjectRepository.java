package com.viraj.dmabackend.project.repository;

import com.viraj.dmabackend.project.entity.Project;
import com.viraj.dmabackend.project.enums.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ProjectRepository extends MongoRepository<Project, String>, ProjectRepositoryCustom {

    Page<Project> findByClientId(String clientId, Pageable pageable);

    Page<Project> findByStatus(ProjectStatus status, Pageable pageable);

    boolean existsByProjectNameAndClientId(String projectName, String clientId);

    boolean existsByProjectNameAndClientIdAndIdNot(String projectName, String clientId, String id);

    Optional<Project> findByIdAndStatusNot(String id, ProjectStatus status);

    Page<Project> findByStatusNot(ProjectStatus status, Pageable pageable);

    Page<Project> findByClientIdAndStatusNot(String clientId, ProjectStatus status, Pageable pageable);
}
