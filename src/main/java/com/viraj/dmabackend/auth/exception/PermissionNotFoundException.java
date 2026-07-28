package com.viraj.dmabackend.auth.exception;

public class PermissionNotFoundException extends RuntimeException {

    public PermissionNotFoundException(String permissionId) {
        super("Permission not found with id: " + permissionId);
    }
}