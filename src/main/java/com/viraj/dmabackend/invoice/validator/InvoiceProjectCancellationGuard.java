package com.viraj.dmabackend.invoice.validator;

import com.viraj.dmabackend.exception.BadRequestException;
import com.viraj.dmabackend.invoice.enums.InvoiceStatus;
import com.viraj.dmabackend.invoice.repository.InvoiceRepository;
import com.viraj.dmabackend.project.validator.ProjectCancellationGuard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvoiceProjectCancellationGuard implements ProjectCancellationGuard {

    private final InvoiceRepository invoiceRepository;

    @Override
    public void validateCanCancelProject(String projectId) {
        String normalizedProjectId = projectId != null ? projectId.trim() : null;
        if (normalizedProjectId != null) {
            if (invoiceRepository.existsByProjectIdAndActiveTrueAndStatusIn(normalizedProjectId,
                    List.of(InvoiceStatus.DRAFT, InvoiceStatus.SENT, InvoiceStatus.OVERDUE))) {
                throw new BadRequestException("Cannot cancel project with open, unpaid, or overdue invoices. Settle or cancel invoices first.");
            }
            if (invoiceRepository.existsByProjectIdAndActiveTrueAndStatus(normalizedProjectId, InvoiceStatus.PAID)) {
                log.warn("Project {} cancelled with paid invoices present. Requires accounting review and reconciliation.", normalizedProjectId);
            }
        }
    }
}
