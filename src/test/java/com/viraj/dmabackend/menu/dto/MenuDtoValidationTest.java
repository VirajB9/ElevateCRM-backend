package com.viraj.dmabackend.menu.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MenuDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void createMenuRequest_ParentIdMax50Enforced() {
        CreateMenuRequest valid = CreateMenuRequest.builder()
                .title("Test Menu")
                .path("/test")
                .icon("icon")
                .orderIndex(1)
                .parentId("a".repeat(50))
                .build();
        assertThat(validator.validate(valid)).isEmpty();

        CreateMenuRequest invalid = CreateMenuRequest.builder()
                .title("Test Menu")
                .path("/test")
                .icon("icon")
                .orderIndex(1)
                .parentId("a".repeat(51))
                .build();
        assertThat(validator.validate(invalid)).isNotEmpty();
    }

    @Test
    void updateMenuRequest_ParentIdMax50Enforced() {
        UpdateMenuRequest valid = UpdateMenuRequest.builder()
                .title("Test Menu")
                .path("/test")
                .icon("icon")
                .orderIndex(1)
                .active(true)
                .parentId("a".repeat(50))
                .version(1L)
                .build();
        assertThat(validator.validate(valid)).isEmpty();

        UpdateMenuRequest invalid = UpdateMenuRequest.builder()
                .title("Test Menu")
                .path("/test")
                .icon("icon")
                .orderIndex(1)
                .active(true)
                .parentId("a".repeat(51))
                .version(1L)
                .build();
        assertThat(validator.validate(invalid)).isNotEmpty();
    }

    @Test
    void updateMenuRequest_nullVersion_failsValidation() {
        UpdateMenuRequest invalid = UpdateMenuRequest.builder()
                .title("Test Menu")
                .path("/test")
                .icon("icon")
                .orderIndex(1)
                .active(true)
                .version(null)
                .build();
        assertThat(validator.validate(invalid)).anyMatch(v -> v.getPropertyPath().toString().equals("version")
                && v.getMessage().contains("Version is required"));
    }
}
