package com.viraj.dmabackend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RemovePermissionsRequest {

    @NotEmpty(message = "At least one permission is required")
    @jakarta.validation.constraints.Size(min = 1, max = 100, message = "Permission IDs must contain between 1 and 100 entries")
    private List<@NotBlank(message = "Permission ID cannot be blank") String> permissionIds;

    @NotNull(message = "Version is required for optimistic concurrency control")
    private Long version;
}
