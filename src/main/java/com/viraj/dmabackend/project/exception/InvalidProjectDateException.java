package com.viraj.dmabackend.project.exception;

public class InvalidProjectDateException extends RuntimeException {

    public InvalidProjectDateException() {
        super("Project end date cannot be earlier than the project start date.");
    }
}
