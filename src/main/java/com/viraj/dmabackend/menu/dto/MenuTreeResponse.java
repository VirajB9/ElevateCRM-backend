package com.viraj.dmabackend.menu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuTreeResponse {

    private String id;

    private String title;

    private String path;

    private String icon;

    private Integer orderIndex;

    private String requiredPermission;

    @Builder.Default
    private List<MenuTreeResponse> children = new ArrayList<>();
}
