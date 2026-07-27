package com.viraj.dmabackend.lead.exception;

public class DuplicateLeadException extends RuntimeException {

    public DuplicateLeadException(String field, String value) {
        super("A lead with " + field + " '" + value + "' already exists.");
    }
}