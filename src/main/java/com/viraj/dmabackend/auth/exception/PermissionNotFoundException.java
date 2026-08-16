package com.viraj.dmabackend.auth.exception;

import com.viraj.dmabackend.exception.ResourceNotFoundException;

public class PermissionNotFoundException extends ResourceNotFoundException {

    public PermissionNotFoundException(String permissionId) {
        super("Permission not found with id: " + permissionId);
    }
}