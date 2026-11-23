package com.viraj.dmabackend.invoice.repository;

import com.viraj.dmabackend.invoice.entity.InvoiceHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface InvoiceHistoryRepository
        extends MongoRepository<InvoiceHistory, String> {

    List<InvoiceHistory> findByInvoiceIdOrderByInvoiceVersionAsc(
            String invoiceId
    );

    Page<InvoiceHistory> findByInvoiceId(
            String invoiceId,
            Pageable pageable
    );

    default Page<InvoiceHistory> findByInvoiceIdOrderByInvoiceVersionAsc(
            String invoiceId,
            Pageable pageable
    ) {
        return findByInvoiceId(invoiceId, pageable);
    }
}