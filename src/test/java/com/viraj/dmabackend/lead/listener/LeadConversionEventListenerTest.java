package com.viraj.dmabackend.lead.listener;

import com.viraj.dmabackend.common.dto.LeadConversionData;
import com.viraj.dmabackend.lead.event.LeadConvertedEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class LeadConversionEventListenerTest {

    private final LeadConversionEventListener listener = new LeadConversionEventListener();

    @Test
    void handleLeadConverted_logsEventWithoutException() {
        LeadConversionData data = LeadConversionData.builder()
                .leadId("lead-123")
                .clientId("client-456")
                .firstName("John")
                .lastName("Doe")
                .companyName("Acme Corp")
                .email("john@acme.com")
                .phoneNumber("+1234567890")
                .build();

        LeadConvertedEvent event = new LeadConvertedEvent(data);

        assertDoesNotThrow(() -> listener.handleLeadConverted(event));
    }

    @Test
    void handleLeadConverted_handlesNullGracefully() {
        assertDoesNotThrow(() -> listener.handleLeadConverted(null));
        assertDoesNotThrow(() -> listener.handleLeadConverted(new LeadConvertedEvent(null)));
    }
}
