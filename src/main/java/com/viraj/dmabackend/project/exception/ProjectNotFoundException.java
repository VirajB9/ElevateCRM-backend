package com.viraj.dmabackend.project.exception;

public class ProjectNotFoundException extends RuntimeException {

    public ProjectNotFoundException(String projectId) {
        super("Project not found with ID: " + projectId);
    }
}