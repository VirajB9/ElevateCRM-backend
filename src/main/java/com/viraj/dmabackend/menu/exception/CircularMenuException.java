package com.viraj.dmabackend.menu.exception;

import com.viraj.dmabackend.exception.BadRequestException;

public class CircularMenuException extends BadRequestException {

    public CircularMenuException() {
        super("Circular parent reference detected. A menu cannot be an ancestor of its own parent.");
    }

    public CircularMenuException(String message) {
        super(message);
    }
}

