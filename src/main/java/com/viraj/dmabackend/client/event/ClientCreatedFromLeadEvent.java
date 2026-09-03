package com.viraj.dmabackend.client.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ClientCreatedFromLeadEvent {
    private final String leadId;
    private final String clientId;
}
