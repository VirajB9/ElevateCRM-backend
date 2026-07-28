package com.viraj.dmabackend.client.repository;

import com.viraj.dmabackend.auth.enums.UserStatus;
import com.viraj.dmabackend.client.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ClientRepository extends MongoRepository<Client, String> {

    Optional<Client> findByEmail(String email);

    Optional<Client> findByPhoneNumber(String phoneNumber);

    Optional<Client> findByGstNumber(String gstNumber);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByGstNumber(String gstNumber);

    Page<Client> findByStatus(UserStatus status, Pageable pageable);

    Page<Client> findByCompanyNameContainingIgnoreCase(String companyName, Pageable pageable);

    boolean existsByEmailAndIdNot(String email, String clientId);

    boolean existsByPhoneNumberAndIdNot(String phoneNumber, String clientId);

    boolean existsByGstNumberAndIdNot(String gstNumber, String clientId);

    Page<Client> findByStatusNot(UserStatus status, Pageable pageable);

    Page<Client> findByCompanyNameContainingIgnoreCaseAndStatusNot(String companyName, UserStatus status, Pageable pageable);
}