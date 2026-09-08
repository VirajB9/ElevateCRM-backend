package com.viraj.dmabackend.menu.exception;

public class CircularMenuException extends RuntimeException {
    public CircularMenuException() {
        super("Circular parent reference detected. A menu cannot be an ancestor of its own parent.");
    }
}
