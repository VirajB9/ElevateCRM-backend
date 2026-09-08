package com.viraj.dmabackend.lead.event;

import com.viraj.dmabackend.common.dto.LeadConversionData;
import lombok.Getter;

@Getter
public class LeadConvertedEvent {

    private final LeadConversionData data;

    public LeadConvertedEvent(LeadConversionData data) {
        this.data = data;
    }
}
