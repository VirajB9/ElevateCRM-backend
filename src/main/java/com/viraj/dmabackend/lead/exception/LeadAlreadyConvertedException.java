package com.viraj.dmabackend.lead.exception;

public class LeadAlreadyConvertedException extends RuntimeException {

    public LeadAlreadyConvertedException(String leadId) {
        super("Lead with ID '" + leadId + "' has already been converted.");
    }
}