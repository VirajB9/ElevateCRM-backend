package com.viraj.dmabackend.invoice.service.impl;

import com.viraj.dmabackend.common.util.PaginationUtils;
import com.viraj.dmabackend.exception.BadRequestException;
import com.viraj.dmabackend.invoice.dto.CreateInvoiceRequest;
import com.viraj.dmabackend.invoice.dto.InvoiceHistoryResponse;
import com.viraj.dmabackend.invoice.dto.InvoiceResponse;
import com.viraj.dmabackend.invoice.dto.UpdateInvoiceRequest;
import com.viraj.dmabackend.invoice.dto.UpdateInvoiceStatusRequest;
import com.viraj.dmabackend.invoice.entity.Invoice;
import com.viraj.dmabackend.invoice.entity.InvoiceHistory;
import com.viraj.dmabackend.invoice.entity.InvoiceItem;
import com.viraj.dmabackend.invoice.enums.InvoiceStatus;
import com.viraj.dmabackend.invoice.exception.InvalidInvoiceStatusException;
import com.viraj.dmabackend.invoice.exception.InvoiceNotFoundException;
import com.viraj.dmabackend.invoice.mapper.InvoiceMapper;
import com.viraj.dmabackend.invoice.repository.InvoiceHistoryRepository;
import com.viraj.dmabackend.invoice.repository.InvoiceRepository;
import com.viraj.dmabackend.invoice.service.InvoiceNumberGenerator;
import com.viraj.dmabackend.invoice.service.InvoiceService;
import com.viraj.dmabackend.invoice.validator.InvoiceValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceMapper invoiceMapper;
    private final InvoiceValidator invoiceValidator;
    private final InvoiceNumberGenerator invoiceNumberGenerator;
    private final InvoiceHistoryRepository invoiceHistoryRepository;

    @Override
    @Transactional
    public InvoiceResponse createInvoice(CreateInvoiceRequest request) {

        invoiceValidator.validateCreateInvoice(request);

        Invoice invoice = invoiceMapper.toEntity(request);

        invoice.setStatus(InvoiceStatus.DRAFT);

        invoice.setActive(true);

        calculateInvoiceTotals(invoice);

        invoice.setInvoiceNumber(invoiceNumberGenerator.generate());

        Invoice savedInvoice = invoiceRepository.save(invoice);

        saveInvoiceHistory(savedInvoice);

        return invoiceMapper.toResponse(savedInvoice);
    }

    @Override
    public InvoiceResponse getInvoiceById(String invoiceId) {

        Invoice invoice = findInvoiceById(invoiceId);

        return invoiceMapper.toResponse(invoice);
    }

    @Override
    public Page<InvoiceHistoryResponse> getInvoiceHistory(String invoiceId, Pageable pageable) {

        String trimmedInvoiceId = invoiceId != null ? invoiceId.trim() : null;
        invoiceRepository.findByIdAndActiveTrue(trimmedInvoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException(invoiceId));

        return invoiceHistoryRepository
                .findByInvoiceIdOrderByInvoiceVersionAsc(trimmedInvoiceId, PaginationUtils.clampPageable(pageable))
                .map(invoiceMapper::toHistoryResponse);
    }

    @Override
    public Page<InvoiceResponse> getAllInvoices(Pageable pageable) {

        return invoiceRepository.findByActiveTrue(PaginationUtils.clampPageable(pageable))
                .map(invoiceMapper::toResponse);
    }

    @Override
    public Page<InvoiceResponse> getInvoicesByStatus(InvoiceStatus status, Pageable pageable) {

        return invoiceRepository.findByActiveTrueAndStatus(status, PaginationUtils.clampPageable(pageable))
                .map(invoiceMapper::toResponse);
    }

    @Override
    public Page<InvoiceResponse> getInvoicesByClientId(String clientId, Pageable pageable) {

        String trimmedClientId = clientId != null ? clientId.trim() : null;
        invoiceValidator.validateClientExists(trimmedClientId);

        return invoiceRepository.findByActiveTrueAndClientId(trimmedClientId, PaginationUtils.clampPageable(pageable))
                .map(invoiceMapper::toResponse);
    }

    @Override
    public Page<InvoiceResponse> getInvoicesByProjectId(String projectId, Pageable pageable) {

        String trimmedProjectId = projectId != null ? projectId.trim() : null;
        invoiceValidator.validateProjectExists(trimmedProjectId);

        return invoiceRepository.findByActiveTrueAndProjectId(trimmedProjectId, PaginationUtils.clampPageable(pageable))
                .map(invoiceMapper::toResponse);
    }

    @Override
    @Transactional
    public InvoiceResponse updateInvoice(String invoiceId, UpdateInvoiceRequest request) {

        Invoice invoice = findInvoiceById(invoiceId);

        if (request.getVersion() != null && !request.getVersion().equals(invoice.getVersion())) {
            throw new OptimisticLockingFailureException(
                    "Invoice has been modified by another user. Please refresh and try again."
            );
        }

        validateInvoiceEditable(invoice);

        invoiceValidator.validateUpdateInvoice(invoice, request);

        invoiceMapper.updateEntity(invoice, request);

        calculateInvoiceTotals(invoice);

        Invoice updatedInvoice = invoiceRepository.save(invoice);

        saveInvoiceHistory(updatedInvoice);

        return invoiceMapper.toResponse(updatedInvoice);
    }

    @Override
    @Transactional
    public InvoiceResponse updateInvoiceStatus(String invoiceId, UpdateInvoiceStatusRequest request) {

        Invoice invoice = findInvoiceById(invoiceId);

        if (request.getVersion() != null && !request.getVersion().equals(invoice.getVersion())) {
            throw new OptimisticLockingFailureException(
                    "Invoice has been modified by another user. Please refresh and try again."
            );
        }

        validateStatusTransition(invoice, request.getStatus());

        invoice.setStatus(request.getStatus());

        if (request.getStatus() == InvoiceStatus.PAID) {
            invoice.setPaidDate(LocalDate.now(ZoneOffset.UTC));
        }

        Invoice updatedInvoice = invoiceRepository.save(invoice);

        saveInvoiceHistory(updatedInvoice);

        return invoiceMapper.toResponse(updatedInvoice);
    }

    @Override
    @Transactional
    public void deleteInvoice(String invoiceId) {

        Invoice invoice = findInvoiceById(invoiceId);

        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new InvalidInvoiceStatusException(
                    "Only draft invoices can be deleted. Issued, paid, overdue, or cancelled invoices must be retained for tax audit compliance."
            );
        }

        invoice.setActive(false);
        invoice.setDeleted(true);

        Invoice deletedInvoice = invoiceRepository.save(invoice);

        saveInvoiceHistory(deletedInvoice);
    }

    // =========================
    // Helper Methods
    // =========================
    private Invoice findInvoiceById(String invoiceId) {

        String trimmedInvoiceId = invoiceId != null ? invoiceId.trim() : null;
        return invoiceRepository.findByIdAndActiveTrue(trimmedInvoiceId)
                .orElseThrow(() ->
                        new InvoiceNotFoundException(invoiceId));
    }

    // Package-private to allow direct unit testing without reflection
    void calculateInvoiceTotals(Invoice invoice) {

        BigDecimal subtotal = BigDecimal.ZERO;

        if (invoice.getItems() != null) {
            for (InvoiceItem item : invoice.getItems()) {
                if (item == null) {
                    continue;
                }

                BigDecimal quantity = Optional.ofNullable(item.getQuantity())
                        .orElse(BigDecimal.ZERO);

                BigDecimal unitPrice = Optional.ofNullable(item.getUnitPrice())
                        .orElse(BigDecimal.ZERO)
                        .setScale(2, RoundingMode.HALF_UP);
                item.setUnitPrice(unitPrice);

                BigDecimal lineTotal = quantity
                        .multiply(unitPrice)
                        .setScale(2, RoundingMode.HALF_UP);

                item.setLineTotal(lineTotal);

                subtotal = subtotal.add(lineTotal);
            }
        }

        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);

        BigDecimal taxPercentage = Optional.ofNullable(invoice.getTaxPercentage())
                .orElse(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
        invoice.setTaxPercentage(taxPercentage);

        BigDecimal discount = Optional.ofNullable(invoice.getDiscount())
                .orElse(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);

        if (discount.compareTo(subtotal) > 0) {
            throw new BadRequestException("Discount cannot be greater than subtotal");
        }

        BigDecimal taxableAmount = subtotal
                .subtract(discount)
                .max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal taxAmount = taxableAmount
                .multiply(taxPercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal totalAmount = taxableAmount
                .add(taxAmount)
                .setScale(2, RoundingMode.HALF_UP);

        invoice.setSubtotal(subtotal);
        invoice.setTaxAmount(taxAmount);
        invoice.setDiscount(discount);
        invoice.setTotalAmount(totalAmount);
    }

    private void validateInvoiceEditable(Invoice invoice) {

        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new InvalidInvoiceStatusException(
                    String.format("Only draft invoices can be edited. Current status is %s.", invoice.getStatus())
            );
        }
    }

    private void validateStatusTransition(Invoice invoice, InvoiceStatus newStatus) {

        InvoiceStatus currentStatus = invoice.getStatus();

        if (currentStatus == newStatus) {
            throw new InvalidInvoiceStatusException(
                    String.format("Invoice is already in %s status.", currentStatus)
            );
        }

        if (currentStatus == InvoiceStatus.PAID) {
            throw new InvalidInvoiceStatusException("Paid invoices cannot change status.");
        }

        if (currentStatus == InvoiceStatus.CANCELLED) {
            throw new InvalidInvoiceStatusException("Cancelled invoices cannot change status.");
        }

        if (newStatus == InvoiceStatus.DRAFT && currentStatus != InvoiceStatus.DRAFT) {
            throw new InvalidInvoiceStatusException("Cannot revert an issued or overdue invoice back to draft.");
        }

        if (currentStatus == InvoiceStatus.OVERDUE && newStatus == InvoiceStatus.SENT) {
            throw new InvalidInvoiceStatusException("Overdue invoices cannot be reverted to sent status.");
        }

        if (newStatus == InvoiceStatus.OVERDUE) {
            if (currentStatus != InvoiceStatus.SENT) {
                throw new InvalidInvoiceStatusException("Only sent invoices can transition to overdue.");
            }
            LocalDate today = LocalDate.now(ZoneOffset.UTC);
            if (invoice.getDueDate() == null || !today.isAfter(invoice.getDueDate())) {
                throw new InvalidInvoiceStatusException("Invoice cannot be marked as overdue before its due date.");
            }
        }

        if (newStatus == InvoiceStatus.PAID
                && currentStatus != InvoiceStatus.SENT
                && currentStatus != InvoiceStatus.OVERDUE) {
            throw new InvalidInvoiceStatusException("Only sent or overdue invoices can be marked as paid");
        }
    }

    private String getCurrentUserEmail() {

        return com.viraj.dmabackend.auth.security.SecurityUtils.getCurrentUserEmail()
                .orElse("SYSTEM");
    }

    private void saveInvoiceHistory(Invoice invoice) {

        InvoiceHistory history = InvoiceHistory.builder()
                .invoiceId(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .clientId(invoice.getClientId())
                .projectId(invoice.getProjectId())
                .issueDate(invoice.getIssueDate())
                .dueDate(invoice.getDueDate())
                .items(copyItems(invoice.getItems()))
                .subtotal(invoice.getSubtotal())
                .taxPercentage(invoice.getTaxPercentage())
                .taxAmount(invoice.getTaxAmount())
                .discount(invoice.getDiscount())
                .totalAmount(invoice.getTotalAmount())
                .status(invoice.getStatus())
                .notes(invoice.getNotes())
                .paidDate(invoice.getPaidDate())
                .active(invoice.getActive())
                .invoiceVersion(invoice.getVersion() != null ? invoice.getVersion() : 0L)
                .changedBy(getCurrentUserEmail())
                .changedAt(LocalDateTime.now(ZoneOffset.UTC))
                .build();

        invoiceHistoryRepository.save(history);
    }

    private List<InvoiceItem> copyItems(List<InvoiceItem> items) {
        if (items == null) {
            return java.util.Collections.emptyList();
        }
        return items.stream()
                .filter(java.util.Objects::nonNull)
                .map(item -> InvoiceItem.builder()
                        .description(item.getDescription())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .lineTotal(item.getLineTotal())
                        .build())
                .toList();
    }
}
