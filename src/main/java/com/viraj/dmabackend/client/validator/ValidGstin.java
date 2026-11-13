package com.viraj.dmabackend.client.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = GstinConstraintValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidGstin {
    String message() default "GST number must be a valid 15-character statutory GSTIN format with valid Modulo-36 checksum";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
