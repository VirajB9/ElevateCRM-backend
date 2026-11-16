package com.viraj.dmabackend.lead.validator;

import com.viraj.dmabackend.client.validator.ClientDeletionGuard;
import com.viraj.dmabackend.exception.BadRequestException;
import com.viraj.dmabackend.lead.repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LeadClientDeletionGuard implements ClientDeletionGuard {

    private final LeadRepository leadRepository;

    @Override
    public void validateCanArchiveClient(String clientId) {
        String trimmedClientId = clientId != null ? clientId.trim() : null;
        if (trimmedClientId != null && leadRepository.existsByConvertedClientIdAndDeletedFalse(trimmedClientId)) {
            throw new BadRequestException("Cannot archive client with associated converted leads.");
        }
    }

    @Override
    public void validateCanDeactivateClient(String clientId) {
        // Deactivation does not break referential integrity for converted leads
    }
}
