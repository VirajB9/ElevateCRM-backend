package com.viraj.dmabackend.auth.exception;

import com.viraj.dmabackend.exception.UnauthorizedException;

public class UnauthorizedRoleAssignmentException extends UnauthorizedException {

    public UnauthorizedRoleAssignmentException(String message) {
        super(message);
    }
}
