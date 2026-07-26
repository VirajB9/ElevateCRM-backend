package com.viraj.dmabackend.project.dto;
import com.viraj.dmabackend.project.enums.ProjectPriority;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProjectRequest {

    @NotBlank(message = "Client ID is required")
    private String clientId;

    @NotBlank(message = "Project name is required")
    private String projectName;

    private String description;

    @NotNull(message = "Priority is required")
    private ProjectPriority priority;

    @FutureOrPresent(message = "Start date cannot be in the past")
    private LocalDate startDate;

    private LocalDate endDate;

    @DecimalMin(value = "0.0", inclusive = true, message = "Budget cannot be negative")
    private BigDecimal budget;

    private String notes;
}
