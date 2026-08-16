package com.viraj.dmabackend.menu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateMenuRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Path is required")
    private String path;

    @NotBlank(message = "Icon is required")
    private String icon;

    private String parentId;

    @NotNull(message = "Order index is required")
    private Integer orderIndex;

    @NotBlank(message = "Required permission is required")
    private String requiredPermission;

    @Builder.Default
    private Boolean active = true;
}
