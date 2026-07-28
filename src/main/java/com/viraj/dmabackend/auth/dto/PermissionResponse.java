package com.viraj.dmabackend.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PermissionResponse {

    private String id;

    private String name;

    private String description;
}