package com.viraj.dmabackend.auth.exception;

public class UnauthorizedRoleAssignmentException extends RuntimeException {

    public UnauthorizedRoleAssignmentException(String message) {
        super(message);
    }
}
