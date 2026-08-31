package com.viraj.dmabackend.project.repository;

import com.viraj.dmabackend.project.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectRepositoryCustom {
    Page<Project> searchProjects(String keyword, Pageable pageable);
}
