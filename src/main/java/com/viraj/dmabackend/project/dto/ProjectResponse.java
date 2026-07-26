package com.viraj.dmabackend.project.dto;

import com.viraj.dmabackend.project.enums.ProjectPriority;
import com.viraj.dmabackend.project.enums.ProjectStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectResponse {

    private String id;

    private String clientId;

    private String projectName;

    private String description;

    private ProjectStatus status;

    private ProjectPriority priority;

    private LocalDate startDate;

    private LocalDate endDate;

    private BigDecimal budget;

    private String notes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String createdBy;

    private String updatedBy;
}
