package com.viraj.dmabackend.project.entity;

import com.viraj.dmabackend.common.entity.BaseEntity;
import com.viraj.dmabackend.project.enums.ProjectPriority;
import com.viraj.dmabackend.project.enums.ProjectStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "projects")
@TypeAlias("Project")
public class Project extends BaseEntity {

    @Id
    private String id;

    @Indexed
    private String clientId;

    @TextIndexed
    private String projectName;

    @TextIndexed
    private String description;

    @Builder.Default @Indexed
    private ProjectStatus status = ProjectStatus.PLANNING;

    @Builder.Default
    private ProjectPriority priority = ProjectPriority.MEDIUM;

    private LocalDate startDate;

    private LocalDate endDate;

    private BigDecimal budget;

    private String notes;
}
