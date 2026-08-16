package com.viraj.dmabackend.auth.exception;

import com.viraj.dmabackend.exception.BadRequestException;

public class DuplicatePermissionException extends BadRequestException {

    public DuplicatePermissionException(String permissionId) {
        super("Duplicate permission found with id: " + permissionId);
    }
}