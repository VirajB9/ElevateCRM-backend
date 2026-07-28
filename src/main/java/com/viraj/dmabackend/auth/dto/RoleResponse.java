package com.viraj.dmabackend.auth.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class RoleResponse {

    private String id;

    private String name;

    private String description;

    private boolean systemRole;

    private List<PermissionResponse> permissions;
}
