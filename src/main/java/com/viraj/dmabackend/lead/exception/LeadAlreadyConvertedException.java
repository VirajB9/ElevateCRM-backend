package com.viraj.dmabackend.lead.exception;

import com.viraj.dmabackend.exception.ConflictException;

public class LeadAlreadyConvertedException extends ConflictException {

    public LeadAlreadyConvertedException(String leadId) {
        super("Lead with ID '" + leadId + "' has already been converted.");
    }
}
