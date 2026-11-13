package com.viraj.dmabackend.project.dto;

import com.viraj.dmabackend.project.enums.ProjectPriority;
import com.viraj.dmabackend.project.enums.ProjectStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void createProjectRequest_withNegativeBudget_failsValidation() {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setClientId("client-1");
        request.setProjectName("ERP System");
        request.setPriority(ProjectPriority.HIGH);
        request.setStartDate(LocalDate.now());
        request.setBudget(new BigDecimal("-100.00"));

        Set<ConstraintViolation<CreateProjectRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("budget")
                && v.getMessage().contains("Budget cannot be negative"));
    }

    @Test
    void createProjectRequest_withZeroOrPositiveBudget_passesValidation() {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setClientId("client-1");
        request.setProjectName("ERP System");
        request.setPriority(ProjectPriority.HIGH);
        request.setStartDate(LocalDate.now());
        request.setBudget(BigDecimal.ZERO);

        Set<ConstraintViolation<CreateProjectRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void updateProjectRequest_withNegativeBudget_failsValidation() {
        UpdateProjectRequest request = new UpdateProjectRequest();
        request.setProjectName("ERP System");
        request.setPriority(ProjectPriority.HIGH);
        request.setStatus(ProjectStatus.ACTIVE);
        request.setStartDate(LocalDate.now());
        request.setBudget(new BigDecimal("-0.01"));
        request.setVersion(1L);

        Set<ConstraintViolation<UpdateProjectRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("budget")
                && v.getMessage().contains("Budget cannot be negative"));
    }

    @Test
    void updateProjectRequest_withoutVersion_failsValidation() {
        UpdateProjectRequest request = new UpdateProjectRequest();
        request.setProjectName("ERP System");
        request.setPriority(ProjectPriority.HIGH);
        request.setStatus(ProjectStatus.ACTIVE);
        request.setStartDate(LocalDate.now());
        request.setVersion(null);

        Set<ConstraintViolation<UpdateProjectRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("version")
                && v.getMessage().contains("Version is required"));
    }
}
