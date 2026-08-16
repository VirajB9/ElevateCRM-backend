package com.viraj.dmabackend.project.repository;

import com.viraj.dmabackend.project.entity.Project;
import com.viraj.dmabackend.project.enums.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProjectRepository extends MongoRepository<Project, String> {

    Page<Project> findByClientId(String clientId, Pageable pageable);

    Page<Project> findByStatus(ProjectStatus status, Pageable pageable);

    Page<Project> findByProjectNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String projectName, String description, Pageable pageable);

    boolean existsByProjectNameAndClientId(String projectName, String clientId);

    boolean existsByProjectNameAndClientIdAndIdNot(String projectName, String clientId, String id);
}