package com.viraj.dmabackend.lead.event;

import com.viraj.dmabackend.common.dto.LeadConversionData;
import lombok.Getter;

@Getter
public class LeadConvertedEvent {

    private final LeadConversionData data;

    public LeadConvertedEvent(LeadConversionData data) {
        this.data = data;
    }

    public String getClientId() {
        return data != null ? data.getClientId() : null;
    }

    public String getLeadId() {
        return data != null ? data.getLeadId() : null;
    }

    public String getAssignedUserId() {
        return data != null ? data.getAssignedUserId() : null;
    }
}
