package com.viraj.dmabackend.project.exception;

import com.viraj.dmabackend.exception.ResourceNotFoundException;

public class ProjectNotFoundException extends ResourceNotFoundException {

    public ProjectNotFoundException(String projectId) {
        super("Project not found with id: " + projectId);
    }
}
