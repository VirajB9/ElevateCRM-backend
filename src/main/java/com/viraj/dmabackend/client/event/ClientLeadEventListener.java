package com.viraj.dmabackend.client.event;

import com.viraj.dmabackend.client.entity.Client;
import com.viraj.dmabackend.client.service.ClientService;
import com.viraj.dmabackend.lead.entity.Lead;
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

        Lead lead = event.getLead();

        Client client = clientService.createClientFromLead(lead);

        eventPublisher.publishEvent(new ClientCreatedFromLeadEvent(lead.getId(), client.getId()));
    }
}
