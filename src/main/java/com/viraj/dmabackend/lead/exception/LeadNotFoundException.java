package com.viraj.dmabackend.lead.exception;

public class LeadNotFoundException extends RuntimeException {

    public LeadNotFoundException(String leadId) {
        super("Lead not found with ID: " + leadId);
    }
}