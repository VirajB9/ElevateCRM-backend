package com.viraj.dmabackend.auth.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AssignPermissionsRequest {

    @NotEmpty(message = "At least one permission is required")
    private List<String> permissionIds;
}