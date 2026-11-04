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
        if (clientRepository.existsByEmailAndStatusNot(email, com.viraj.dmabackend.client.enums.ClientStatus.ARCHIVED)) {
            throw new DuplicateClientEmailException(email);
        }
    }

    public void validateDuplicatePhone(String phoneNumber) {
        if (clientRepository.existsByPhoneNumberAndStatusNot(phoneNumber, com.viraj.dmabackend.client.enums.ClientStatus.ARCHIVED)) {
            throw new DuplicateClientPhoneException(phoneNumber);
        }
    }

    public void validateDuplicateGst(String gstNumber) {
        if (gstNumber != null && !gstNumber.isBlank() && clientRepository.existsByGstNumberAndStatusNot(gstNumber, com.viraj.dmabackend.client.enums.ClientStatus.ARCHIVED)) {
            throw new DuplicateClientGstException(gstNumber);
        }
    }

    public void validateEmailForUpdate(String email, String clientId) {
        if (clientRepository.existsByEmailAndIdNotAndStatusNot(email, clientId, com.viraj.dmabackend.client.enums.ClientStatus.ARCHIVED)) {
            throw new DuplicateClientEmailException(email);
        }
    }

    public void validatePhoneForUpdate(String phoneNumber, String clientId) {
        if (clientRepository.existsByPhoneNumberAndIdNotAndStatusNot(phoneNumber, clientId, com.viraj.dmabackend.client.enums.ClientStatus.ARCHIVED)) {
            throw new DuplicateClientPhoneException(phoneNumber);
        }
    }

    public void validateGstForUpdate(String gstNumber, String clientId) {
        if (gstNumber != null && !gstNumber.isBlank() && clientRepository.existsByGstNumberAndIdNotAndStatusNot(gstNumber, clientId, com.viraj.dmabackend.client.enums.ClientStatus.ARCHIVED)) {
            throw new DuplicateClientGstException(gstNumber);
        }
    }

    public void validateClientDeletable(String clientId) {
        if (projectRepository.existsByClientIdAndStatusIn(clientId, 
                List.of(ProjectStatus.PLANNING, ProjectStatus.ACTIVE, ProjectStatus.ON_HOLD))) {
            throw new BadRequestException("Cannot archive client with active or ongoing projects.");
        }

        if (invoiceRepository.existsByClientIdAndActiveTrueAndStatusIn(clientId, 
                List.of(InvoiceStatus.DRAFT, InvoiceStatus.SENT, InvoiceStatus.OVERDUE))) {
            throw new BadRequestException("Cannot archive client with unpaid or active invoices.");
        }
    }

    public void validateClientStatusForUpdate(com.viraj.dmabackend.client.enums.ClientStatus status) {
        if (status == com.viraj.dmabackend.client.enums.ClientStatus.ARCHIVED) {
            throw new BadRequestException("Clients cannot be archived via update. Please use the delete client endpoint.");
        }
    }
}
