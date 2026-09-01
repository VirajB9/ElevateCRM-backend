package com.viraj.dmabackend.invoice.repository;

import com.viraj.dmabackend.invoice.entity.InvoiceHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface InvoiceHistoryRepository
        extends MongoRepository<InvoiceHistory, String> {

    List<InvoiceHistory> findByInvoiceIdOrderByInvoiceVersionAsc(
            String invoiceId
    );
}