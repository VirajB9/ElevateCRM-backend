package com.viraj.dmabackend.client.validator;

import com.viraj.dmabackend.client.exception.DuplicateClientEmailException;
import com.viraj.dmabackend.client.exception.DuplicateClientGstException;
import com.viraj.dmabackend.client.exception.DuplicateClientPhoneException;
import com.viraj.dmabackend.client.repository.ClientRepository;
import com.viraj.dmabackend.exception.BadRequestException;
import com.viraj.dmabackend.invoice.enums.InvoiceStatus;
import com.viraj.dmabackend.invoice.repository.InvoiceRepository;
import com.viraj.dmabackend.project.enums.ProjectStatus;
import com.viraj.dmabackend.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ClientValidator {

    private final ClientRepository clientRepository;
    private final ProjectRepository projectRepository;
    private final InvoiceRepository invoiceRepository;

    public void validateDuplicateEmail(String email) {
        String normalizedEmail = email != null ? email.trim().toLowerCase() : null;
        if (clientRepository.existsByEmailAndStatusNot(normalizedEmail, com.viraj.dmabackend.client.enums.ClientStatus.ARCHIVED)) {
            throw new DuplicateClientEmailException(email);
        }
    }

    public void validateDuplicatePhone(String phoneNumber) {
        String normalizedPhone = phoneNumber != null ? phoneNumber.trim() : null;
        if (normalizedPhone != null && clientRepository.existsByPhoneNumberAndStatusNot(normalizedPhone, com.viraj.dmabackend.client.enums.ClientStatus.ARCHIVED)) {
            throw new DuplicateClientPhoneException(phoneNumber);
        }
    }

    public void validateDuplicateGst(String gstNumber) {
        String normalizedGst = gstNumber != null && !gstNumber.isBlank() ? gstNumber.trim().toUpperCase() : null;
        if (normalizedGst != null && clientRepository.existsByGstNumberAndStatusNot(normalizedGst, com.viraj.dmabackend.client.enums.ClientStatus.ARCHIVED)) {
            throw new DuplicateClientGstException(gstNumber);
        }
    }

    public void validateEmailForUpdate(String email, String clientId) {
        String normalizedEmail = email != null ? email.trim().toLowerCase() : null;
        String trimmedClientId = clientId != null ? clientId.trim() : null;
        if (clientRepository.existsByEmailAndIdNotAndStatusNot(normalizedEmail, trimmedClientId, com.viraj.dmabackend.client.enums.ClientStatus.ARCHIVED)) {
            throw new DuplicateClientEmailException(email);
        }
    }

    public void validatePhoneForUpdate(String phoneNumber, String clientId) {
        String normalizedPhone = phoneNumber != null ? phoneNumber.trim() : null;
        String trimmedClientId = clientId != null ? clientId.trim() : null;
        if (normalizedPhone != null && clientRepository.existsByPhoneNumberAndIdNotAndStatusNot(normalizedPhone, trimmedClientId, com.viraj.dmabackend.client.enums.ClientStatus.ARCHIVED)) {
            throw new DuplicateClientPhoneException(phoneNumber);
        }
    }

    public void validateGstForUpdate(String gstNumber, String clientId) {
        String normalizedGst = gstNumber != null && !gstNumber.isBlank() ? gstNumber.trim().toUpperCase() : null;
        String trimmedClientId = clientId != null ? clientId.trim() : null;
        if (normalizedGst != null && clientRepository.existsByGstNumberAndIdNotAndStatusNot(normalizedGst, trimmedClientId, com.viraj.dmabackend.client.enums.ClientStatus.ARCHIVED)) {
            throw new DuplicateClientGstException(gstNumber);
        }
    }

    public void validateClientDeletable(String clientId) {
        String trimmedClientId = clientId != null ? clientId.trim() : null;
        if (projectRepository.existsByClientIdAndStatusIn(trimmedClientId, 
                List.of(ProjectStatus.PLANNING, ProjectStatus.ACTIVE, ProjectStatus.ON_HOLD))) {
            throw new BadRequestException("Cannot archive client with active or ongoing projects.");
        }

        if (invoiceRepository.existsByClientIdAndActiveTrueAndStatusIn(trimmedClientId, 
                List.of(InvoiceStatus.DRAFT, InvoiceStatus.SENT, InvoiceStatus.OVERDUE))) {
            throw new BadRequestException("Cannot archive client with unpaid or active invoices.");
        }
    }

    public void validateClientStatusForUpdate(com.viraj.dmabackend.client.enums.ClientStatus status) {
        validateClientStatusForUpdate(status, null);
    }

    public void validateClientStatusForUpdate(com.viraj.dmabackend.client.enums.ClientStatus status, String clientId) {
        if (status == com.viraj.dmabackend.client.enums.ClientStatus.ARCHIVED) {
            throw new BadRequestException("Clients cannot be archived via update. Please use the delete client endpoint.");
        }

        String trimmedClientId = clientId != null ? clientId.trim() : null;
        if (status == com.viraj.dmabackend.client.enums.ClientStatus.INACTIVE && trimmedClientId != null) {
            if (projectRepository.existsByClientIdAndStatusIn(trimmedClientId,
                    List.of(ProjectStatus.PLANNING, ProjectStatus.ACTIVE, ProjectStatus.ON_HOLD))) {
                throw new BadRequestException("Cannot deactivate client with active or ongoing projects.");
            }

            if (invoiceRepository.existsByClientIdAndActiveTrueAndStatusIn(trimmedClientId,
                    List.of(InvoiceStatus.DRAFT, InvoiceStatus.SENT, InvoiceStatus.OVERDUE))) {
                throw new BadRequestException("Cannot deactivate client with unpaid or active invoices.");
            }
        }
    }
}
