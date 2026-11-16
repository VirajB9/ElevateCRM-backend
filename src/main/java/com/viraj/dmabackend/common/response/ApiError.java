package com.viraj.dmabackend.common.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

@Schema(description = "Standard API error response payload")
public record ApiError(
        @Schema(description = "HTTP status code", example = "400")
        int status,

        @Schema(description = "Machine-readable standardized error code", example = "VALIDATION_FAILED")
        String errorCode,

        @Schema(description = "Human-readable error description", example = "Validation failed")
        String message,

        @Schema(description = "Field-specific validation errors when applicable", example = "{\"email\": \"Invalid email format\"}")
        Map<String, String> fieldErrors,

        @Schema(description = "Error timestamp in UTC ISO-8601")
        Instant timestamp
) {
    public static ApiError of(int status, String errorCode, String message) {
        return new ApiError(status, errorCode, message, null, Instant.now());
    }

    public static ApiError of(int status, String errorCode, String message, Map<String, String> fieldErrors) {
        return new ApiError(status, errorCode, message, fieldErrors, Instant.now());
    }
}
