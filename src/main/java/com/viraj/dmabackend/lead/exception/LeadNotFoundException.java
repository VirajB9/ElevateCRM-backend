package com.viraj.dmabackend.lead.exception;

import com.viraj.dmabackend.exception.ResourceNotFoundException;

public class LeadNotFoundException extends ResourceNotFoundException {

    public LeadNotFoundException(String leadId) {
        super("Lead not found with id: " + leadId);
    }
}