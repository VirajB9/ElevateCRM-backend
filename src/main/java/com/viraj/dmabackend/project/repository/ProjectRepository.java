package com.viraj.dmabackend.project.repository;

import com.viraj.dmabackend.project.entity.Project;
import com.viraj.dmabackend.project.enums.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface ProjectRepository extends MongoRepository<Project, String>, ProjectRepositoryCustom {

    @Query("{ 'clientId': ?0, 'deleted': false }")
    Page<Project> findByClientId(String clientId, Pageable pageable);

    @Query("{ 'status': ?0, 'deleted': false }")
    Page<Project> findByStatus(ProjectStatus status, Pageable pageable);

    @Query(value = "{ 'projectName': ?0, 'clientId': ?1, 'deleted': false }", exists = true)
    boolean existsByProjectNameAndClientId(String projectName, String clientId);

    @Query(value = "{ 'projectName': ?0, 'clientId': ?1, 'status': { $ne: ?2 }, 'deleted': false }", exists = true)
    boolean existsByProjectNameAndClientIdAndStatusNot(String projectName, String clientId, ProjectStatus status);

    @Query(value = "{ 'projectName': ?0, 'clientId': ?1, '_id': { $ne: ?2 }, 'deleted': false }", exists = true)
    boolean existsByProjectNameAndClientIdAndIdNot(String projectName, String clientId, String id);

    @Query(value = "{ 'projectName': ?0, 'clientId': ?1, '_id': { $ne: ?2 }, 'status': { $ne: ?3 }, 'deleted': false }", exists = true)
    boolean existsByProjectNameAndClientIdAndIdNotAndStatusNot(String projectName, String clientId, String id, ProjectStatus status);

    @Query("{ '_id': ?0, 'status': { $ne: ?1 }, 'deleted': false }")
    Optional<Project> findByIdAndStatusNot(String id, ProjectStatus status);

    @Query("{ 'status': { $ne: ?0 }, 'deleted': false }")
    Page<Project> findByStatusNot(ProjectStatus status, Pageable pageable);

    @Query("{ 'clientId': ?0, 'status': { $ne: ?1 }, 'deleted': false }")
    Page<Project> findByClientIdAndStatusNot(String clientId, ProjectStatus status, Pageable pageable);

    @Query(value = "{ 'clientId': ?0, 'status': { $in: ?1 }, 'deleted': false }", exists = true)
    boolean existsByClientIdAndStatusIn(String clientId, java.util.Collection<ProjectStatus> statuses);
}
