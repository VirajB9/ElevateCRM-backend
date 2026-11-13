package com.viraj.dmabackend.invoice.validator;

import com.viraj.dmabackend.exception.BadRequestException;
import com.viraj.dmabackend.invoice.enums.InvoiceStatus;
import com.viraj.dmabackend.invoice.repository.InvoiceRepository;
import com.viraj.dmabackend.project.validator.ProjectCompletionGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InvoiceProjectCompletionGuard implements ProjectCompletionGuard {

    private final InvoiceRepository invoiceRepository;

    @Override
    public void validateCanCompleteProject(String projectId) {
        String normalizedProjectId = projectId != null ? projectId.trim() : null;
        if (normalizedProjectId != null && invoiceRepository.existsByProjectIdAndActiveTrueAndStatusIn(normalizedProjectId,
                List.of(InvoiceStatus.DRAFT, InvoiceStatus.SENT, InvoiceStatus.OVERDUE))) {
            throw new BadRequestException("Cannot complete project with open, unpaid, or overdue invoices. Settle or cancel invoices first.");
        }
    }
}
