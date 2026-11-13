package com.viraj.dmabackend.project.validator;

public interface ProjectCompletionGuard {
    void validateCanCompleteProject(String projectId);
}
