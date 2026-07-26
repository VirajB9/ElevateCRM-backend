package com.viraj.dmabackend.project.entity;

import com.viraj.dmabackend.common.entity.BaseEntity;
import com.viraj.dmabackend.project.enums.ProjectPriority;
import com.viraj.dmabackend.project.enums.ProjectStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "projects")
public class Project extends BaseEntity {

    @Id
    private String id;

    @NotBlank(message = "Client ID is required")
    private String clientId;

    @NotBlank(message = "Project name is required")
    private String projectName;

    private String description;

    @Builder.Default
    private ProjectStatus status = ProjectStatus.PLANNING;

    @Builder.Default
    private ProjectPriority priority = ProjectPriority.MEDIUM;

    private LocalDate startDate;

    private LocalDate endDate;

    @DecimalMin(value = "0.0", inclusive = true, message = "Budget cannot be negative")
    private BigDecimal budget;

    private String notes;
}
