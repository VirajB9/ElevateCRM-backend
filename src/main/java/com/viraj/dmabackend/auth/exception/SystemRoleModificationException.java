package com.viraj.dmabackend.auth.exception;

import com.viraj.dmabackend.exception.BadRequestException;

public class SystemRoleModificationException extends BadRequestException {

    public SystemRoleModificationException(String roleName) {
        super("System role cannot be modified: " + roleName);
    }
}