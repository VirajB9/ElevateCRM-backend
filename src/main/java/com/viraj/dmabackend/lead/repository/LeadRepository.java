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

    @Query(value = "{ 'email': ?0, 'activePipeline': true }", exists = true)
    boolean existsByEmail(String email);

    @Query(value = "{ 'phoneNumber': ?0, 'activePipeline': true }", exists = true)
    boolean existsByPhoneNumber(String phoneNumber);

    @Query(value = "{ 'email': ?0, 'activePipeline': true }", exists = true)
    boolean existsByEmailAndStatusNot(String email, LeadStatus status);

    @Query(value = "{ 'phoneNumber': ?0, 'activePipeline': true }", exists = true)
    boolean existsByPhoneNumberAndStatusNot(String phoneNumber, LeadStatus status);

    @Query(value = "{ 'email': ?0, 'activePipeline': true }", exists = true)
    boolean existsByEmailAndStatusNotIn(String email, java.util.Collection<LeadStatus> statuses);

    @Query(value = "{ 'phoneNumber': ?0, 'activePipeline': true }", exists = true)
    boolean existsByPhoneNumberAndStatusNotIn(String phoneNumber, java.util.Collection<LeadStatus> statuses);

    @Query(value = "{ 'email': ?0, 'activePipeline': true }", exists = true)
    boolean existsByEmailAndActivePipelineTrue(String email);

    @Query(value = "{ 'phoneNumber': ?0, 'activePipeline': true }", exists = true)
    boolean existsByPhoneNumberAndActivePipelineTrue(String phoneNumber);

    @Query("{ 'email': ?0, 'activePipeline': true }")
    Optional<Lead> findByEmail(String email);

    @Query("{ 'phoneNumber': ?0, 'activePipeline': true }")
    Optional<Lead> findByPhoneNumber(String phoneNumber);

    @Query("{ 'email': ?0, 'activePipeline': true }")
    Optional<Lead> findByEmailAndStatusNot(String email, LeadStatus status);

    @Query("{ 'phoneNumber': ?0, 'activePipeline': true }")
    Optional<Lead> findByPhoneNumberAndStatusNot(String phoneNumber, LeadStatus status);

    @Query("{ 'email': ?0, 'activePipeline': true }")
    Optional<Lead> findByEmailAndStatusNotIn(String email, java.util.Collection<LeadStatus> statuses);

    @Query("{ 'phoneNumber': ?0, 'activePipeline': true }")
    Optional<Lead> findByPhoneNumberAndStatusNotIn(String phoneNumber, java.util.Collection<LeadStatus> statuses);

    @Query("{ 'email': ?0, 'activePipeline': true }")
    Optional<Lead> findByEmailAndActivePipelineTrue(String email);

    @Query("{ 'phoneNumber': ?0, 'activePipeline': true }")
    Optional<Lead> findByPhoneNumberAndActivePipelineTrue(String phoneNumber);

    @Query("{ 'status': ?0, 'deleted': false }")
    Page<Lead> findByStatus(LeadStatus status, Pageable pageable);

    @Query("{ 'status': ?0, 'assignedUserId': ?1, 'deleted': false }")
    Page<Lead> findByStatusAndAssignedUserId(LeadStatus status, String assignedUserId, Pageable pageable);

    @Query("{ 'assignedUserId': ?0, 'deleted': false }")
    Page<Lead> findByAssignedUserId(String assignedUserId, Pageable pageable);

    @Query("{ 'source': ?0, 'deleted': false }")
    Page<Lead> findBySource(LeadSource source, Pageable pageable);

    @Query("{ 'source': ?0, 'assignedUserId': ?1, 'status': { $ne: ?2 }, 'deleted': false }")
    Page<Lead> findBySourceAndAssignedUserIdAndStatusNot(LeadSource source, String assignedUserId, LeadStatus status, Pageable pageable);

    @Query("{ 'source': ?0, 'status': { $ne: ?1 }, 'deleted': false }")
    Page<Lead> findBySourceAndStatusNot(LeadSource source, LeadStatus status, Pageable pageable);

    @Query("{ 'assignedUserId': ?0, 'status': { $ne: ?1 }, 'deleted': false }")
    Page<Lead> findByAssignedUserIdAndStatusNot(String assignedUserId, LeadStatus status, Pageable pageable);

    @Query("{ '_id': ?0, 'status': { $ne: ?1 }, 'deleted': false }")
    Optional<Lead> findByIdAndStatusNot(String id, LeadStatus status);

    @Query("{ 'status': { $ne: ?0 }, 'deleted': false }")
    Page<Lead> findByStatusNot(LeadStatus status, Pageable pageable);

    @Query(value = "{ 'assignedUserId': ?0, 'status': { $in: ?1 }, 'deleted': false }", exists = true)
    boolean existsByAssignedUserIdAndStatusIn(String assignedUserId, java.util.Collection<LeadStatus> statuses);
}
