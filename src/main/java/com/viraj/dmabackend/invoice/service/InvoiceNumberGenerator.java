package com.viraj.dmabackend.invoice.service;

import com.viraj.dmabackend.common.counter.CounterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneOffset;

@Component
@RequiredArgsConstructor
public class InvoiceNumberGenerator {

    private static final String INVOICE_COUNTER = "invoice";
    private static final String INVOICE_PREFIX = "INV";

    private final CounterRepository counterRepository;

    public String generate() {
        return generate(null);
    }

    public String generate(LocalDate issueDate) {
        int year = (issueDate != null) ? issueDate.getYear() : Year.now(ZoneOffset.UTC).getValue();
        String counterKey = INVOICE_COUNTER + "_" + year;

        long sequence = counterRepository.getNextSequence(counterKey);

        return String.format(
                "%s-%d-%05d",
                INVOICE_PREFIX,
                year,
                sequence);
    }

    public void compensate(String invoiceNumber) {
        // Monotonic sequence counters are never decremented backwards on transaction abort
        // to prevent duplicate key collisions and preserve monotonic audit logs.
    }
}
