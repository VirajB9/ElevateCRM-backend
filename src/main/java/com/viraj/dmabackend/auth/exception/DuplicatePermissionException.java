package com.viraj.dmabackend.auth.exception;

import com.viraj.dmabackend.exception.ConflictException;

public class DuplicatePermissionException extends ConflictException {

    public DuplicatePermissionException(String permissionId) {
        super("DUPLICATE_PERMISSION", "Duplicate permission found with id: " + permissionId);
    }
}
