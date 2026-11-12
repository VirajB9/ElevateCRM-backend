package com.viraj.dmabackend.invoice.validator;

import com.viraj.dmabackend.exception.BadRequestException;
import com.viraj.dmabackend.invoice.enums.InvoiceStatus;
import com.viraj.dmabackend.invoice.repository.InvoiceRepository;
import com.viraj.dmabackend.project.validator.ProjectDeletionGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InvoiceProjectDeletionGuard implements ProjectDeletionGuard {

    private final InvoiceRepository invoiceRepository;

    @Override
    public void validateCanArchiveProject(String projectId) {
        String normalizedProjectId = projectId != null ? projectId.trim() : null;
        if (normalizedProjectId != null && invoiceRepository.existsByProjectIdAndActiveTrueAndStatusIn(normalizedProjectId,
                List.of(InvoiceStatus.DRAFT, InvoiceStatus.SENT, InvoiceStatus.OVERDUE))) {
            throw new BadRequestException("Cannot archive project with unpaid or active invoices.");
        }
    }
}
