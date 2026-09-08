package com.viraj.dmabackend.client.event;

import com.viraj.dmabackend.client.entity.Client;
import com.viraj.dmabackend.client.service.ClientService;
import com.viraj.dmabackend.common.dto.LeadConversionData;
import com.viraj.dmabackend.client.event.ClientCreatedFromLeadEvent;
import com.viraj.dmabackend.lead.event.LeadConvertedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClientLeadEventListener {

    private final ClientService clientService;
    private final ApplicationEventPublisher eventPublisher;

    @EventListener
    public void handleLeadConverted(LeadConvertedEvent event) {
        LeadConversionData data = event.getData();
        Client client = clientService.createClientFromLead(data);
        eventPublisher.publishEvent(new ClientCreatedFromLeadEvent(data.getLeadId(), client.getId()));
    }
}
