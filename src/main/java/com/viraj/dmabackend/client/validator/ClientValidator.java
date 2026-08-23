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
        if (clientRepository.existsByEmail(email)) {
            throw new DuplicateClientEmailException(email);
        }
    }

    public void validateDuplicatePhone(String phoneNumber) {
        if (clientRepository.existsByPhoneNumber(phoneNumber)) {
            throw new DuplicateClientPhoneException(phoneNumber);
        }
    }

    public void validateDuplicateGst(String gstNumber) {
        if (gstNumber != null && !gstNumber.isBlank() && clientRepository.existsByGstNumber(gstNumber)) {
            throw new DuplicateClientGstException(gstNumber);
        }
    }

    public void validateEmailForUpdate(String email, String clientId) {
        if (clientRepository.existsByEmailAndIdNot(email, clientId)) {
            throw new DuplicateClientEmailException(email);
        }
    }

    public void validatePhoneForUpdate(String phoneNumber, String clientId) {
        if (clientRepository.existsByPhoneNumberAndIdNot(phoneNumber, clientId)) {
            throw new DuplicateClientPhoneException(phoneNumber);
        }
    }

    public void validateGstForUpdate(String gstNumber, String clientId) {
        if (gstNumber != null && !gstNumber.isBlank() && clientRepository.existsByGstNumberAndIdNot(gstNumber, clientId)) {
            throw new DuplicateClientGstException(gstNumber);
        }
    }
}
