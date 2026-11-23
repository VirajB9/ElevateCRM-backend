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
    @jakarta.validation.constraints.Size(max = 100)
    private String title;

    @NotBlank(message = "Path is required")
    @jakarta.validation.constraints.Size(max = 255)
    @jakarta.validation.constraints.Pattern(regexp = "^(/[a-zA-Z0-9_.-]*)*$", message = "Path must be a valid URI path")
    private String path;

    @NotBlank(message = "Icon is required")
    @jakarta.validation.constraints.Size(max = 100)
    private String icon;

    @jakarta.validation.constraints.Size(max = 50)
    private String parentId;

    @NotNull(message = "Order index is required")
    @jakarta.validation.constraints.Min(value = 0, message = "Order index cannot be negative")
    @jakarta.validation.constraints.Max(value = 10000, message = "Order index cannot exceed 10000")
    private Integer orderIndex;

    @jakarta.validation.constraints.Size(max = 100)
    private String requiredPermission;

    @Builder.Default
    private Boolean active = true;
}
