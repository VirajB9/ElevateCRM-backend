package com.viraj.dmabackend.project.dto;

import com.viraj.dmabackend.project.enums.ProjectPriority;
import com.viraj.dmabackend.project.enums.ProjectStatus;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProjectRequest {

    private String projectName;

    private String description;

    private ProjectStatus status;

    private ProjectPriority priority;

    private LocalDate startDate;

    private LocalDate endDate;

    @DecimalMin(value = "0.0", inclusive = true, message = "Budget cannot be negative")
    private BigDecimal budget;

    private String notes;
}
