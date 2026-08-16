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

        long sequence = counterRepository.getNextSequence(INVOICE_COUNTER);

        return String.format(
                "%s-%d-%05d",
                INVOICE_PREFIX,
                Year.now().getValue(),
                sequence);
    }
}
