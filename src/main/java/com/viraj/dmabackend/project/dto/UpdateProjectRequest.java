package com.viraj.dmabackend.project.dto;

import com.viraj.dmabackend.project.enums.ProjectPriority;
import com.viraj.dmabackend.project.enums.ProjectStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class UpdateProjectRequest {

    @NotBlank(message = "Project name is required")
    @Size(max = 150)
    private String projectName;

    @Size(max = 2000)
    private String description;

    @NotNull(message = "Priority is required")
    private ProjectPriority priority;

    @NotNull(message = "Status is required")
    private ProjectStatus status;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

    @DecimalMin(value = "0.0", inclusive = true, message = "Budget cannot be negative")
    private BigDecimal budget;

    @Size(max = 2000)
    private String notes;

    @NotNull(message = "Version is required for optimistic concurrency control")
    private Long version;
}
