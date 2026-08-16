package com.viraj.dmabackend.auth.exception;

import com.viraj.dmabackend.exception.ResourceNotFoundException;

public class RoleNotFoundException extends ResourceNotFoundException {

    public RoleNotFoundException(String roleId) {
        super("Role not found with id: " + roleId);
    }
}
