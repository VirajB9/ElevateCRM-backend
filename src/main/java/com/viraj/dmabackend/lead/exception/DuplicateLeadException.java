package com.viraj.dmabackend.lead.exception;

import com.viraj.dmabackend.exception.BadRequestException;

public class DuplicateLeadException extends BadRequestException {

    public DuplicateLeadException(String field, String value) {
        super("A lead with " + field + " '" + value + "' already exists.");
    }
}