package com.viraj.dmabackend.client.repository;

import com.viraj.dmabackend.client.enums.ClientStatus;
import com.viraj.dmabackend.client.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ClientRepository extends MongoRepository<Client, String>, ClientRepositoryCustom {

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByGstNumber(String gstNumber);

    Optional<Client> findByEmail(String email);

    Optional<Client> findByPhoneNumber(String phoneNumber);

    Optional<Client> findByGstNumber(String gstNumber);

    Page<Client> findByStatus(ClientStatus status, Pageable pageable);

    boolean existsByEmailAndIdNot(String email, String clientId);

    boolean existsByPhoneNumberAndIdNot(String phoneNumber, String clientId);

    boolean existsByGstNumberAndIdNot(String gstNumber, String clientId);

    Page<Client> findByStatusNot(ClientStatus status, Pageable pageable);
}
