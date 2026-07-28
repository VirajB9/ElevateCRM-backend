package com.viraj.dmabackend.auth.exception;

public class DuplicatePermissionException extends RuntimeException {

    public DuplicatePermissionException(String permissionId) {
        super("Duplicate permission found with id: " + permissionId);
    }
}