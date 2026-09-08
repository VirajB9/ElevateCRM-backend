package com.viraj.dmabackend.invoice.service;

import com.viraj.dmabackend.common.counter.CounterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Year;

@Component
@RequiredArgsConstructor
public class InvoiceNumberGenerator {

    private static final String INVOICE_COUNTER = "invoice";
    private static final String INVOICE_PREFIX = "INV";

    private final CounterRepository counterRepository;

    public String generate() {

        int currentYear = Year.now().getValue();
        String counterKey = INVOICE_COUNTER + "_" + currentYear;

        long sequence = counterRepository.getNextSequence(counterKey);

        return String.format(
                "%s-%d-%05d",
                INVOICE_PREFIX,
                currentYear,
                sequence);
    }
}
