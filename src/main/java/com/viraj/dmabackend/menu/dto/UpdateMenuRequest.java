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
public class UpdateMenuRequest {

    @NotBlank(message = "Title is required")
    @jakarta.validation.constraints.Size(max = 100)
    private String title;

    @NotBlank(message = "Path is required")
    @jakarta.validation.constraints.Size(max = 255)
    private String path;

    @NotBlank(message = "Icon is required")
    @jakarta.validation.constraints.Size(max = 100)
    private String icon;

    @jakarta.validation.constraints.Size(max = 50)
    private String parentId;

    @NotNull(message = "Order index is required")
    private Integer orderIndex;

    @jakarta.validation.constraints.Size(max = 100)
    private String requiredPermission;

    @NotNull(message = "Active status is required")
    private Boolean active;

    @NotNull(message = "Version is required for optimistic concurrency control")
    private Long version;
}
