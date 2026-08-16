package com.viraj.dmabackend.project.exception;

import com.viraj.dmabackend.exception.BadRequestException;

public class DuplicateProjectException extends BadRequestException {

    public DuplicateProjectException(String projectName, String clientId) {
        super("A project with the name '" + projectName + "' already exists for client ID: " + clientId);
    }
}
