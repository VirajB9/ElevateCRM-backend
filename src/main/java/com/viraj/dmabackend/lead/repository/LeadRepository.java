package com.viraj.dmabackend.lead.repository;

import com.viraj.dmabackend.lead.entity.Lead;
import com.viraj.dmabackend.lead.enums.LeadSource;
import com.viraj.dmabackend.lead.enums.LeadStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface LeadRepository extends MongoRepository<Lead, String>, LeadRepositoryCustom {

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    @Query(value = "{ 'email': ?0, 'status': { $ne: ?1 }, 'deleted': false }", exists = true)
    boolean existsByEmailAndStatusNot(String email, LeadStatus status);

    @Query(value = "{ 'phoneNumber': ?0, 'status': { $ne: ?1 }, 'deleted': false }", exists = true)
    boolean existsByPhoneNumberAndStatusNot(String phoneNumber, LeadStatus status);

    Optional<Lead> findByEmail(String email);

    Optional<Lead> findByPhoneNumber(String phoneNumber);

    @Query("{ 'email': ?0, 'status': { $ne: ?1 }, 'deleted': false }")
    Optional<Lead> findByEmailAndStatusNot(String email, LeadStatus status);

    @Query("{ 'phoneNumber': ?0, 'status': { $ne: ?1 }, 'deleted': false }")
    Optional<Lead> findByPhoneNumberAndStatusNot(String phoneNumber, LeadStatus status);

    Page<Lead> findByStatus(LeadStatus status, Pageable pageable);

    Page<Lead> findByAssignedUserId(String assignedUserId, Pageable pageable);

    Page<Lead> findBySource(LeadSource source, Pageable pageable);

    @Query("{ 'source': ?0, 'status': { $ne: ?1 }, 'deleted': false }")
    Page<Lead> findBySourceAndStatusNot(LeadSource source, LeadStatus status, Pageable pageable);

    @Query("{ 'assignedUserId': ?0, 'status': { $ne: ?1 }, 'deleted': false }")
    Page<Lead> findByAssignedUserIdAndStatusNot(String assignedUserId, LeadStatus status, Pageable pageable);

    @Query("{ '_id': ?0, 'status': { $ne: ?1 }, 'deleted': false }")
    Optional<Lead> findByIdAndStatusNot(String id, LeadStatus status);

    @Query("{ 'status': { $ne: ?0 }, 'deleted': false }")
    Page<Lead> findByStatusNot(LeadStatus status, Pageable pageable);

    boolean existsByAssignedUserIdAndStatusIn(String assignedUserId, java.util.Collection<LeadStatus> statuses);
}
