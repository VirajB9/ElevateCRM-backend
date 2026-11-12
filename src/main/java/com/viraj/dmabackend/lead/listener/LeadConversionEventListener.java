package com.viraj.dmabackend.lead.listener;

import com.viraj.dmabackend.lead.event.LeadConvertedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LeadConversionEventListener {

    @EventListener
    public void handleLeadConverted(LeadConvertedEvent event) {
        if (event != null && event.getData() != null) {
            log.info("LeadConvertedEvent processed: Lead [{}] successfully converted to Client [{}]",
                    event.getData().getLeadId(), event.getData().getClientId());
        }
    }
}
