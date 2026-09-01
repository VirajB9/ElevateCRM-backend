package com.viraj.dmabackend.invoice.repository;

import com.viraj.dmabackend.invoice.entity.Invoice;
import com.viraj.dmabackend.invoice.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends MongoRepository<Invoice, String> {

    Page<Invoice> findByActiveTrue(Pageable pageable);

    Page<Invoice> findByActiveTrueAndStatus(InvoiceStatus status, Pageable pageable);

    Page<Invoice> findByActiveTrueAndClientId(String clientId, Pageable pageable);

    Page<Invoice> findByActiveTrueAndProjectId(String projectId, Pageable pageable);

    Optional<Invoice> findByIdAndActiveTrue(String id);
}
