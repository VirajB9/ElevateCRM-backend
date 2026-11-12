package com.viraj.dmabackend.lead.exception;

import com.viraj.dmabackend.exception.ConflictException;

public class DuplicateLeadException extends ConflictException {

    public DuplicateLeadException(String field, String value) {
        super("A lead with " + field + " '" + value + "' already exists.");
    }
}
