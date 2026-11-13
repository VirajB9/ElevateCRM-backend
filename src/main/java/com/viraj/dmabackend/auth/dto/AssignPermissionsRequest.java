package com.viraj.dmabackend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AssignPermissionsRequest {

    @NotEmpty(message = "At least one permission is required")
    private List<@NotBlank(message = "Permission ID cannot be blank") String> permissionIds;

    @NotNull(message = "Version is required for optimistic concurrency control")
    private Long version;
}
