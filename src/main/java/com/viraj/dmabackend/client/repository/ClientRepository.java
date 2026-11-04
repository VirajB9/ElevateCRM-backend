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

    boolean existsByEmailAndStatusNot(String email, ClientStatus status);

    boolean existsByPhoneNumberAndStatusNot(String phoneNumber, ClientStatus status);

    boolean existsByGstNumberAndStatusNot(String gstNumber, ClientStatus status);

    Optional<Client> findByEmail(String email);

    Optional<Client> findByPhoneNumber(String phoneNumber);

    Optional<Client> findByGstNumber(String gstNumber);

    Optional<Client> findByEmailAndStatusNot(String email, ClientStatus status);

    Optional<Client> findByPhoneNumberAndStatusNot(String phoneNumber, ClientStatus status);

    Optional<Client> findByGstNumberAndStatusNot(String gstNumber, ClientStatus status);

    Page<Client> findByStatus(ClientStatus status, Pageable pageable);

    boolean existsByEmailAndIdNot(String email, String clientId);

    boolean existsByPhoneNumberAndIdNot(String phoneNumber, String clientId);

    boolean existsByGstNumberAndIdNot(String gstNumber, String clientId);

    boolean existsByEmailAndIdNotAndStatusNot(String email, String clientId, ClientStatus status);

    boolean existsByPhoneNumberAndIdNotAndStatusNot(String phoneNumber, String clientId, ClientStatus status);

    boolean existsByGstNumberAndIdNotAndStatusNot(String gstNumber, String clientId, ClientStatus status);

    Page<Client> findByStatusNot(ClientStatus status, Pageable pageable);

    Optional<Client> findByIdAndStatusNot(String id, ClientStatus status);
    boolean existsByIdAndStatusNot(String id, ClientStatus status);
}
