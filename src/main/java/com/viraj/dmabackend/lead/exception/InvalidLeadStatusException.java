package com.viraj.dmabackend.lead.exception;

import com.viraj.dmabackend.exception.BadRequestException;
import com.viraj.dmabackend.lead.enums.LeadStatus;

public class InvalidLeadStatusException extends BadRequestException {

    public InvalidLeadStatusException(LeadStatus currentStatus, LeadStatus targetStatus) {
        super("Cannot change lead status from '" + currentStatus + "' to '" + targetStatus + "'.");
    }
}
