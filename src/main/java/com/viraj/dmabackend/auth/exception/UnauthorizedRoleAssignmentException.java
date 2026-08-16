package com.viraj.dmabackend.auth.exception;

import com.viraj.dmabackend.exception.BadRequestException;

public class UnauthorizedRoleAssignmentException extends BadRequestException {

    public UnauthorizedRoleAssignmentException(String message) {
        super(message);
    }
}
