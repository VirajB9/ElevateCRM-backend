package com.viraj.dmabackend.project.dto;

import com.viraj.dmabackend.project.enums.ProjectPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CreateProjectRequest {

    @NotBlank(message = "Client ID is required")
    @Size(max = 50)
    private String clientId;

    @NotBlank(message = "Project name is required")
    @Size(max = 150)
    private String projectName;

    @Size(max = 2000)
    private String description;

    @NotNull(message = "Priority is required")
    private ProjectPriority priority;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

    private BigDecimal budget;

    @Size(max = 2000)
    private String notes;
}
