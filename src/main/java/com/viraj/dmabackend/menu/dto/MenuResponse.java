package com.viraj.dmabackend.menu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuResponse {

    private String id;

    private String title;

    private String path;

    private String icon;

    private String parentId;

    private Integer orderIndex;

    private String requiredPermission;

    private Boolean active;
}
