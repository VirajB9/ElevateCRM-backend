package com.viraj.dmabackend.auth.exception;

public class SystemRoleModificationException extends RuntimeException {

    public SystemRoleModificationException(String roleName) {
        super("System role cannot be modified: " + roleName);
    }
}