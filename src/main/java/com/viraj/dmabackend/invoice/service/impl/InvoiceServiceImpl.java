package com.viraj.dmabackend.invoice.service.impl;

import com.viraj.dmabackend.exception.BadRequestException;
import com.viraj.dmabackend.invoice.dto.CreateInvoiceRequest;
import com.viraj.dmabackend.invoice.dto.InvoiceResponse;
import com.viraj.dmabackend.invoice.dto.UpdateInvoiceRequest;
import com.viraj.dmabackend.invoice.dto.UpdateInvoiceStatusRequest;
import com.viraj.dmabackend.invoice.entity.Invoice;
import com.viraj.dmabackend.invoice.entity.InvoiceItem;
import com.viraj.dmabackend.invoice.enums.InvoiceStatus;
import com.viraj.dmabackend.invoice.exception.InvalidInvoiceStatusException;
import com.viraj.dmabackend.invoice.exception.InvoiceNotFoundException;
import com.viraj.dmabackend.invoice.mapper.InvoiceMapper;
import com.viraj.dmabackend.invoice.repository.InvoiceRepository;
import com.viraj.dmabackend.invoice.service.InvoiceNumberGenerator;
import com.viraj.dmabackend.invoice.service.InvoiceService;
import com.viraj.dmabackend.invoice.validator.InvoiceValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceMapper invoiceMapper;
    private final InvoiceValidator invoiceValidator;
    private final InvoiceNumberGenerator invoiceNumberGenerator;

    @Override
    public InvoiceResponse createInvoice(CreateInvoiceRequest request) {

        invoiceValidator.validateCreateInvoice(request);

        Invoice invoice = invoiceMapper.toEntity(request);

        invoice.setInvoiceNumber(invoiceNumberGenerator.generate());

        invoice.setStatus(InvoiceStatus.DRAFT);

        invoice.setActive(true);

        calculateInvoiceTotals(invoice);

        Invoice savedInvoice = invoiceRepository.save(invoice);

        return invoiceMapper.toResponse(savedInvoice);
    }

    @Override
    public InvoiceResponse getInvoiceById(String invoiceId) {

        Invoice invoice = findInvoiceById(invoiceId);

        return invoiceMapper.toResponse(invoice);
    }

    @Override
    public Page<InvoiceResponse> getAllInvoices(Pageable pageable) {

        return invoiceRepository.findByActiveTrue(pageable)
                .map(invoiceMapper::toResponse);
    }

    @Override
    public Page<InvoiceResponse> getInvoicesByStatus(InvoiceStatus status, Pageable pageable) {

        return invoiceRepository.findByActiveTrueAndStatus(status, pageable)
                .map(invoiceMapper::toResponse);
    }

    @Override
    public Page<InvoiceResponse> getInvoicesByClientId(String clientId, Pageable pageable) {

        return invoiceRepository.findByActiveTrueAndClientId(clientId, pageable)
                .map(invoiceMapper::toResponse);
    }

    @Override
    public Page<InvoiceResponse> getInvoicesByProjectId(String projectId, Pageable pageable) {

        return invoiceRepository.findByActiveTrueAndProjectId(projectId, pageable)
                .map(invoiceMapper::toResponse);
    }

    @Override
    public InvoiceResponse updateInvoice(String invoiceId, UpdateInvoiceRequest request) {

        Invoice invoice = findInvoiceById(invoiceId);

        validateInvoiceEditable(invoice);

        invoiceValidator.validateUpdateInvoice(invoice, request);

        invoiceMapper.updateEntity(invoice, request);

        calculateInvoiceTotals(invoice);

        Invoice updatedInvoice = invoiceRepository.save(invoice);

        return invoiceMapper.toResponse(updatedInvoice);
    }

    @Override
    public InvoiceResponse updateInvoiceStatus(String invoiceId, UpdateInvoiceStatusRequest request) {

        Invoice invoice = findInvoiceById(invoiceId);

        validateStatusTransition(invoice.getStatus(), request.getStatus());

        invoice.setStatus(request.getStatus());

        if (request.getStatus() == InvoiceStatus.PAID) {
            invoice.setPaidDate(LocalDate.now());
        }

        Invoice updatedInvoice = invoiceRepository.save(invoice);

        return invoiceMapper.toResponse(updatedInvoice);
    }

    @Override
    public void deleteInvoice(String invoiceId) {

        Invoice invoice = findInvoiceById(invoiceId);

        invoice.setActive(false);

        invoiceRepository.save(invoice);
    }

    // =========================
    // Helper Methods
    // =========================
    private Invoice findInvoiceById(String invoiceId) {

        return invoiceRepository.findById(invoiceId)
                .orElseThrow(() ->
                        new InvoiceNotFoundException(invoiceId));
    }

    private void calculateInvoiceTotals(Invoice invoice) {

        BigDecimal subtotal = BigDecimal.ZERO;

        for (InvoiceItem item : invoice.getItems()) {

            BigDecimal lineTotal = item.getQuantity()
                    .multiply(item.getUnitPrice())
                    .setScale(2, RoundingMode.HALF_UP);

            item.setLineTotal(lineTotal);

            subtotal = subtotal.add(lineTotal);
        }

        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);

        BigDecimal taxAmount = subtotal
                .multiply(invoice.getTaxPercentage())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal discount = invoice.getDiscount()
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalAmount = subtotal
                .add(taxAmount)
                .subtract(discount)
                .setScale(2, RoundingMode.HALF_UP);

        invoice.setSubtotal(subtotal);
        invoice.setTaxAmount(taxAmount);
        invoice.setDiscount(discount);
        invoice.setTotalAmount(totalAmount);
    }

    private void validateInvoiceEditable(Invoice invoice) {

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new InvalidInvoiceStatusException("Paid invoices cannot be edited.");
        }

        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new InvalidInvoiceStatusException("Cancelled invoices cannot be edited.");
        }
    }

    private void validateStatusTransition(InvoiceStatus currentStatus, InvoiceStatus newStatus) {

        if (currentStatus == InvoiceStatus.PAID) {
            throw new InvalidInvoiceStatusException("Paid invoices cannot change status.");
        }

        if (currentStatus == InvoiceStatus.CANCELLED) {
            throw new InvalidInvoiceStatusException("Cancelled invoices cannot change status.");
        }

        if (newStatus == InvoiceStatus.PAID
                && currentStatus != InvoiceStatus.SENT
                && currentStatus != InvoiceStatus.OVERDUE) {
            throw new InvalidInvoiceStatusException("Only sent or overdue invoices can be marked as paid");
        }
    }
}
