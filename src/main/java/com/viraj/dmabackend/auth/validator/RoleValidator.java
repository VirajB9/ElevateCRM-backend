package com.viraj.dmabackend.auth.validator;

import com.viraj.dmabackend.auth.exception.DuplicatePermissionException;
import com.viraj.dmabackend.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class RoleValidator {

    public void validatePermissionIds(List<String> permissionIds) {

        Set<String> uniquePermissionIds = new HashSet<>();

        for (String permissionId : permissionIds) {

            if (!uniquePermissionIds.add(permissionId)) {
                throw new DuplicatePermissionException(permissionId);
            }
        }
    }

    public void validateDescription(String description) {

        if (description == null || description.trim().isEmpty()) {
            throw new BadRequestException("Role description cannot be empty.");
        }
    }
}
