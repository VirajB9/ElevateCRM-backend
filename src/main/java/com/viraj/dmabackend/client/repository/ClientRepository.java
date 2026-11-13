package com.viraj.dmabackend.client.repository;

import com.viraj.dmabackend.client.enums.ClientStatus;
import com.viraj.dmabackend.client.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface ClientRepository extends MongoRepository<Client, String>, ClientRepositoryCustom {

    @Query(value = "{ 'email': ?0, 'deleted': false }", exists = true)
    boolean existsByEmail(String email);

    @Query(value = "{ 'phoneNumber': ?0, 'deleted': false }", exists = true)
    boolean existsByPhoneNumber(String phoneNumber);

    @Query(value = "{ 'gstNumber': ?0, 'deleted': false, 'gstNumber': { $type: 'string' } }", exists = true)
    boolean existsByGstNumber(String gstNumber);

    @Query(value = "{ 'email': ?0, 'status': { $ne: ?1 }, 'deleted': false }", exists = true)
    boolean existsByEmailAndStatusNot(String email, ClientStatus status);

    @Query(value = "{ 'phoneNumber': ?0, 'status': { $ne: ?1 }, 'deleted': false }", exists = true)
    boolean existsByPhoneNumberAndStatusNot(String phoneNumber, ClientStatus status);

    @Query(value = "{ 'gstNumber': ?0, 'status': { $ne: ?1 }, 'deleted': false, 'gstNumber': { $type: 'string' } }", exists = true)
    boolean existsByGstNumberAndStatusNot(String gstNumber, ClientStatus status);

    @Query("{ 'email': ?0, 'deleted': false }")
    Optional<Client> findByEmail(String email);

    @Query("{ 'phoneNumber': ?0, 'deleted': false }")
    Optional<Client> findByPhoneNumber(String phoneNumber);

    @Query("{ 'gstNumber': ?0, 'deleted': false, 'gstNumber': { $type: 'string' } }")
    Optional<Client> findByGstNumber(String gstNumber);

    @Query("{ 'email': ?0, 'status': { $ne: ?1 }, 'deleted': false }")
    Optional<Client> findByEmailAndStatusNot(String email, ClientStatus status);

    @Query("{ 'phoneNumber': ?0, 'status': { $ne: ?1 }, 'deleted': false }")
    Optional<Client> findByPhoneNumberAndStatusNot(String phoneNumber, ClientStatus status);

    @Query("{ 'gstNumber': ?0, 'status': { $ne: ?1 }, 'deleted': false, 'gstNumber': { $type: 'string' } }")
    Optional<Client> findByGstNumberAndStatusNot(String gstNumber, ClientStatus status);

    @Query("{ 'status': ?0, 'deleted': false }")
    Page<Client> findByStatus(ClientStatus status, Pageable pageable);

    @Query(value = "{ 'email': ?0, '_id': { $ne: ?1 }, 'deleted': false }", exists = true)
    boolean existsByEmailAndIdNot(String email, String clientId);

    @Query(value = "{ 'phoneNumber': ?0, '_id': { $ne: ?1 }, 'deleted': false }", exists = true)
    boolean existsByPhoneNumberAndIdNot(String phoneNumber, String clientId);

    @Query(value = "{ 'gstNumber': ?0, '_id': { $ne: ?1 }, 'deleted': false, 'gstNumber': { $type: 'string' } }", exists = true)
    boolean existsByGstNumberAndIdNot(String gstNumber, String clientId);

    @Query(value = "{ 'email': ?0, '_id': { $ne: ?1 }, 'status': { $ne: ?2 }, 'deleted': false }", exists = true)
    boolean existsByEmailAndIdNotAndStatusNot(String email, String clientId, ClientStatus status);

    @Query(value = "{ 'phoneNumber': ?0, '_id': { $ne: ?1 }, 'status': { $ne: ?2 }, 'deleted': false }", exists = true)
    boolean existsByPhoneNumberAndIdNotAndStatusNot(String phoneNumber, String clientId, ClientStatus status);

    @Query(value = "{ 'gstNumber': ?0, '_id': { $ne: ?1 }, 'status': { $ne: ?2 }, 'deleted': false, 'gstNumber': { $type: 'string' } }", exists = true)
    boolean existsByGstNumberAndIdNotAndStatusNot(String gstNumber, String clientId, ClientStatus status);

    @Query("{ 'status': { $ne: ?0 }, 'deleted': false }")
    Page<Client> findByStatusNot(ClientStatus status, Pageable pageable);

    @Query("{ '_id': ?0, 'status': { $ne: ?1 }, 'deleted': false }")
    Optional<Client> findByIdAndStatusNot(String id, ClientStatus status);

    @Query(value = "{ '_id': ?0, 'status': { $ne: ?1 }, 'deleted': false }", exists = true)
    boolean existsByIdAndStatusNot(String id, ClientStatus status);
}
