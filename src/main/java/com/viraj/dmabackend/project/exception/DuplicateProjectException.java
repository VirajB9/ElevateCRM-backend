package com.viraj.dmabackend.project.exception;

import com.viraj.dmabackend.exception.ConflictException;

public class DuplicateProjectException extends ConflictException {

    public DuplicateProjectException(String projectName, String clientId) {
        super("A project with the name '" + projectName + "' already exists for client ID: " + clientId);
    }
}
