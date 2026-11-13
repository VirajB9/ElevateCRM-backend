package com.viraj.dmabackend.menu.dto;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
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
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"
)
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
