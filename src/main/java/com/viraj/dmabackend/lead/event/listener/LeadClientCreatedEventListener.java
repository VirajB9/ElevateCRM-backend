package com.viraj.dmabackend.lead.event.listener;

import com.viraj.dmabackend.client.event.ClientCreatedFromLeadEvent;
import com.viraj.dmabackend.lead.entity.Lead;
import com.viraj.dmabackend.lead.repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LeadClientCreatedEventListener {

    private final LeadRepository leadRepository;

    @EventListener
    public void handleClientCreatedFromLead(ClientCreatedFromLeadEvent event) {

        Lead lead = leadRepository.findById(event.getLeadId())
                .orElseThrow(() -> new RuntimeException("Lead not found: " + event.getLeadId()));

        lead.setConvertedClientId(event.getClientId());

        leadRepository.save(lead);
    }
}
