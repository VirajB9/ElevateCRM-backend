package com.viraj.dmabackend.project.exception;

import com.viraj.dmabackend.exception.BadRequestException;

public class InvalidProjectDateException extends BadRequestException {

    public InvalidProjectDateException() {
        super("Project end date cannot be earlier than the project start date.");
    }
}
