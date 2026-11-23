package com.viraj.dmabackend.exception;

public class ConflictException extends RuntimeException {

    private final String errorCode;

    public ConflictException(String message) {
        super(message);
        this.errorCode = "CONFLICT";
    }

    public ConflictException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode != null ? errorCode : "CONFLICT";
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "CONFLICT";
    }

    public String getErrorCode() {
        return errorCode;
    }
}
