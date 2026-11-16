package com.viraj.dmabackend.invoice.repository;

import com.viraj.dmabackend.invoice.entity.Invoice;
import com.viraj.dmabackend.invoice.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends MongoRepository<Invoice, String> {

    Page<Invoice> findByDeletedFalse(Pageable pageable);

    Page<Invoice> findByDeletedFalseAndStatus(InvoiceStatus status, Pageable pageable);

    Page<Invoice> findByDeletedFalseAndClientId(String clientId, Pageable pageable);

    Page<Invoice> findByDeletedFalseAndProjectId(String projectId, Pageable pageable);

    Optional<Invoice> findByIdAndDeletedFalse(String id);

    boolean existsByClientIdAndDeletedFalseAndStatusIn(String clientId, java.util.Collection<InvoiceStatus> statuses);

    boolean existsByProjectIdAndDeletedFalseAndStatusIn(String projectId, java.util.Collection<InvoiceStatus> statuses);

    boolean existsByProjectIdAndDeletedFalseAndStatus(String projectId, InvoiceStatus status);

    default Page<Invoice> findByActiveTrue(Pageable pageable) {
        return findByDeletedFalse(pageable);
    }

    default Page<Invoice> findByActiveTrueAndStatus(InvoiceStatus status, Pageable pageable) {
        return findByDeletedFalseAndStatus(status, pageable);
    }

    default Page<Invoice> findByActiveTrueAndClientId(String clientId, Pageable pageable) {
        return findByDeletedFalseAndClientId(clientId, pageable);
    }

    default Page<Invoice> findByActiveTrueAndProjectId(String projectId, Pageable pageable) {
        return findByDeletedFalseAndProjectId(projectId, pageable);
    }

    default Optional<Invoice> findByIdAndActiveTrue(String id) {
        return findByIdAndDeletedFalse(id);
    }

    default boolean existsByClientIdAndActiveTrueAndStatusIn(String clientId, java.util.Collection<InvoiceStatus> statuses) {
        return existsByClientIdAndDeletedFalseAndStatusIn(clientId, statuses);
    }

    default boolean existsByProjectIdAndActiveTrueAndStatusIn(String projectId, java.util.Collection<InvoiceStatus> statuses) {
        return existsByProjectIdAndDeletedFalseAndStatusIn(projectId, statuses);
    }

    default boolean existsByProjectIdAndActiveTrueAndStatus(String projectId, InvoiceStatus status) {
        return existsByProjectIdAndDeletedFalseAndStatus(projectId, status);
    }
}
