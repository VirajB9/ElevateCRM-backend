package com.viraj.dmabackend.auth.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void refreshTokenRequest_EnforcesMax500Characters() {
        RefreshTokenRequest valid = new RefreshTokenRequest("a".repeat(500));
        assertThat(validator.validate(valid)).isEmpty();

        RefreshTokenRequest invalid = new RefreshTokenRequest("a".repeat(501));
        assertThat(validator.validate(invalid)).isNotEmpty();
    }

    @Test
    void logoutRequest_EnforcesMax500Characters() {
        LogoutRequest valid = new LogoutRequest("a".repeat(500));
        assertThat(validator.validate(valid)).isEmpty();

        LogoutRequest invalid = new LogoutRequest("a".repeat(501));
        assertThat(validator.validate(invalid)).isNotEmpty();
    }
}
