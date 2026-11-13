package com.viraj.dmabackend.client.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class GstinConstraintValidator implements ConstraintValidator<ValidGstin, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return GstinValidator.isValidGstin(value);
    }
}
