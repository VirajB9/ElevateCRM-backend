package com.viraj.dmabackend.client.validator;

import com.viraj.dmabackend.client.exception.DuplicateClientEmailException;
import com.viraj.dmabackend.client.exception.DuplicateClientGstException;
import com.viraj.dmabackend.client.exception.DuplicateClientPhoneException;
import com.viraj.dmabackend.client.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClientValidator {

    private final ClientRepository clientRepository;

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
}
