package com.viraj.dmabackend.lead.repository;

import com.viraj.dmabackend.lead.entity.Lead;
import com.viraj.dmabackend.lead.enums.LeadSource;
import com.viraj.dmabackend.lead.enums.LeadStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface LeadRepository extends MongoRepository<Lead, String>, LeadRepositoryCustom {

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    Optional<Lead> findByEmail(String email);

    Optional<Lead> findByPhoneNumber(String phoneNumber);

    Page<Lead> findByStatus(LeadStatus status, Pageable pageable);

    Page<Lead> findByAssignedUserId(String assignedUserId, Pageable pageable);

    Page<Lead> findBySource(LeadSource source, Pageable pageable);

    Optional<Lead> findByIdAndStatusNot(String id, LeadStatus status);
}
