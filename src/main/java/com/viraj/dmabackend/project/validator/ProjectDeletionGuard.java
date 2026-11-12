package com.viraj.dmabackend.project.validator;

public interface ProjectDeletionGuard {
    void validateCanArchiveProject(String projectId);
}
