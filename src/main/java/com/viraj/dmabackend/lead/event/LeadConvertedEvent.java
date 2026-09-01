package com.viraj.dmabackend.lead.event;

import com.viraj.dmabackend.lead.entity.Lead;
import lombok.Getter;

@Getter
public class LeadConvertedEvent {

    private final Lead lead;

    public LeadConvertedEvent(Lead lead) {
        this.lead = lead;
    }
}
