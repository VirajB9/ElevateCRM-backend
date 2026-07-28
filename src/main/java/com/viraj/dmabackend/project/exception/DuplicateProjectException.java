package com.viraj.dmabackend.project.exception;

public class DuplicateProjectException extends RuntimeException {

    public DuplicateProjectException(String projectName, String clientId) {
        super("A project with the name '" + projectName + "' already exists for client ID: " + clientId);
    }
}
