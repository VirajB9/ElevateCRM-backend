package com.viraj.dmabackend.lead.listener;

import com.viraj.dmabackend.lead.event.LeadConvertedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class LeadConversionEventListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleLeadConverted(LeadConvertedEvent event) {
        if (event != null && event.getData() != null) {
            log.info("LeadConvertedEvent processed: Lead [{}] successfully converted to Client [{}]",
                    event.getData().getLeadId(), event.getData().getClientId());
        }
    }
}
