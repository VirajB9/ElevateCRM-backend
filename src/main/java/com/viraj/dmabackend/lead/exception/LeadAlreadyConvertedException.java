package com.viraj.dmabackend.lead.exception;

import com.viraj.dmabackend.exception.BadRequestException;

public class LeadAlreadyConvertedException extends BadRequestException {

    public LeadAlreadyConvertedException(String leadId) {
        super("Lead with ID '" + leadId + "' has already been converted.");
    }
}