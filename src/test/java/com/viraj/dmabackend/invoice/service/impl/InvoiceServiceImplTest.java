package com.viraj.dmabackend.invoice.service.impl;

import com.viraj.dmabackend.client.repository.ClientRepository;
import com.viraj.dmabackend.invoice.dto.InvoiceResponse;
import com.viraj.dmabackend.invoice.entity.Invoice;
import com.viraj.dmabackend.invoice.entity.InvoiceHistory;
import com.viraj.dmabackend.invoice.entity.InvoiceItem;
import com.viraj.dmabackend.invoice.enums.InvoiceStatus;
import com.viraj.dmabackend.invoice.exception.InvalidInvoiceStatusException;
import com.viraj.dmabackend.invoice.mapper.InvoiceMapper;
import com.viraj.dmabackend.invoice.repository.InvoiceHistoryRepository;
import com.viraj.dmabackend.invoice.repository.InvoiceRepository;
import com.viraj.dmabackend.invoice.service.InvoiceNumberGenerator;
import com.viraj.dmabackend.invoice.validator.InvoiceValidator;
import com.viraj.dmabackend.project.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InvoiceServiceImplTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InvoiceHistoryRepository invoiceHistoryRepository;

    @Mock
    private InvoiceMapper invoiceMapper;

    @Mock
    private InvoiceValidator invoiceValidator;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private InvoiceNumberGenerator invoiceNumberGenerator;

    @InjectMocks
    private InvoiceServiceImpl invoiceService;

    @Test
    void calculateInvoiceTotals_WithDiscountAndTax() {
        Invoice invoice = new Invoice();
        invoice.setTaxPercentage(new BigDecimal("18.0"));
        invoice.setDiscount(new BigDecimal("2000.0"));
        
        InvoiceItem item = new InvoiceItem();
        item.setQuantity(new BigDecimal("1"));
        item.setUnitPrice(new BigDecimal("10000.0"));
        invoice.setItems(List.of(item));

        invoiceService.calculateInvoiceTotals(invoice);

        assertEquals(new BigDecimal("1440.00"), invoice.getTaxAmount());
        assertEquals(new BigDecimal("9440.00"), invoice.getTotalAmount());
        assertEquals(new BigDecimal("10000.00"), invoice.getSubtotal());
    }

    @Test
    void calculateInvoiceTotals_NoDiscount() {
        Invoice invoice = new Invoice();
        invoice.setTaxPercentage(new BigDecimal("10.0"));
        
        InvoiceItem item = new InvoiceItem();
        item.setQuantity(new BigDecimal("1"));
        item.setUnitPrice(new BigDecimal("5000.0"));
        invoice.setItems(List.of(item));

        invoiceService.calculateInvoiceTotals(invoice);

        assertEquals(new BigDecimal("500.00"), invoice.getTaxAmount());
        assertEquals(new BigDecimal("5500.00"), invoice.getTotalAmount());
        assertEquals(new BigDecimal("0.00"), invoice.getDiscount());
    }

    @Test
    void calculateInvoiceTotals_100PercentDiscount() {
        Invoice invoice = new Invoice();
        invoice.setTaxPercentage(new BigDecimal("18.0"));
        invoice.setDiscount(new BigDecimal("5000.0")); // Subtotal will be 5000, discount is 5000
        
        InvoiceItem item = new InvoiceItem();
        item.setQuantity(new BigDecimal("1"));
        item.setUnitPrice(new BigDecimal("5000.0"));
        invoice.setItems(List.of(item));

        invoiceService.calculateInvoiceTotals(invoice);

        assertEquals(new BigDecimal("0.00"), invoice.getTaxAmount());
        assertEquals(new BigDecimal("0.00"), invoice.getTotalAmount());
    }

    @Test
    void calculateInvoiceTotals_whenItemsListIsNull_calculatesZeroTotalsWithoutNPE() {
        Invoice invoice = new Invoice();
        invoice.setItems(null);

        invoiceService.calculateInvoiceTotals(invoice);

        assertEquals(new BigDecimal("0.00"), invoice.getSubtotal());
        assertEquals(new BigDecimal("0.00"), invoice.getTotalAmount());
    }

    @Test
    void calculateInvoiceTotals_whenItemsContainNullElement_ignoresNullAndCalculatesTotals() {
        Invoice invoice = new Invoice();
        InvoiceItem item = new InvoiceItem();
        item.setQuantity(new BigDecimal("2"));
        item.setUnitPrice(new BigDecimal("50.00"));
        
        java.util.List<InvoiceItem> items = new java.util.ArrayList<>();
        items.add(null);
        items.add(item);
        invoice.setItems(items);

        invoiceService.calculateInvoiceTotals(invoice);

        assertEquals(new BigDecimal("100.00"), invoice.getSubtotal());
        assertEquals(new BigDecimal("100.00"), invoice.getTotalAmount());
    }

    @Test
    void calculateInvoiceTotals_NormalizesTaxPercentageAndUnitPriceScale() {
        Invoice invoice = new Invoice();
        invoice.setTaxPercentage(new BigDecimal("18.5555"));
        invoice.setDiscount(BigDecimal.ZERO);

        InvoiceItem item = new InvoiceItem();
        item.setQuantity(new BigDecimal("2"));
        item.setUnitPrice(new BigDecimal("100.1234"));
        invoice.setItems(List.of(item));

        invoiceService.calculateInvoiceTotals(invoice);

        assertEquals(new BigDecimal("18.56"), invoice.getTaxPercentage());
        assertEquals(new BigDecimal("100.12"), item.getUnitPrice());
        assertEquals(new BigDecimal("200.24"), item.getLineTotal());
    }

    @Test
    void deleteInvoice_ThrowsWhenStatusIsPaid() {
        Invoice invoice = new Invoice();
        invoice.setId("inv-123");
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setActive(true);

        when(invoiceRepository.findByIdAndActiveTrue("inv-123")).thenReturn(Optional.of(invoice));

        assertThrows(InvalidInvoiceStatusException.class, () -> invoiceService.deleteInvoice("inv-123"));
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void deleteInvoice_SuccessWhenStatusIsDraft() {
        Invoice invoice = new Invoice();
        invoice.setId("inv-123");
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setActive(true);

        when(invoiceRepository.findByIdAndActiveTrue("inv-123")).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        invoiceService.deleteInvoice("inv-123");

        assertFalse(invoice.getActive());
        assertTrue(invoice.isDeleted());
        verify(invoiceRepository).save(invoice);
        verify(invoiceHistoryRepository).save(any());
    }

    @Test
    void updateInvoiceStatus_SavesUpdatedStatusInHistory() {
        Invoice invoice = new Invoice();
        invoice.setId("inv-123");
        invoice.setStatus(InvoiceStatus.SENT);
        invoice.setActive(true);

        when(invoiceRepository.findByIdAndActiveTrue("inv-123")).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        com.viraj.dmabackend.invoice.dto.UpdateInvoiceStatusRequest req =
                new com.viraj.dmabackend.invoice.dto.UpdateInvoiceStatusRequest();
        req.setStatus(InvoiceStatus.PAID);

        invoiceService.updateInvoiceStatus("inv-123", req);

        org.mockito.ArgumentCaptor<com.viraj.dmabackend.invoice.entity.InvoiceHistory> captor =
                org.mockito.ArgumentCaptor.forClass(com.viraj.dmabackend.invoice.entity.InvoiceHistory.class);
        verify(invoiceHistoryRepository).save(captor.capture());
        assertEquals(InvoiceStatus.PAID, captor.getValue().getStatus());
        assertEquals(java.time.LocalDate.now(java.time.ZoneOffset.UTC), captor.getValue().getPaidDate());
        assertNotNull(captor.getValue().getChangedAt());
    }

    @Test
    void getInvoiceHistory_ReturnsHistoryList() {
        Invoice invoice = new Invoice();
        invoice.setId("inv-123");
        invoice.setActive(true);

        when(invoiceRepository.findById("inv-123")).thenReturn(Optional.of(invoice));

        com.viraj.dmabackend.invoice.entity.InvoiceHistory history =
                new com.viraj.dmabackend.invoice.entity.InvoiceHistory();
        history.setInvoiceId("inv-123");
        history.setInvoiceVersion(0L);

        when(invoiceHistoryRepository.findByInvoiceIdOrderByInvoiceVersionAsc("inv-123"))
                .thenReturn(List.of(history));

        var result = invoiceService.getInvoiceHistory("inv-123");
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getInvoiceHistory_ReturnsHistoryList_WhenInvoiceIsSoftDeleted() {
        Invoice deletedInvoice = new Invoice();
        deletedInvoice.setId("inv-deleted");
        deletedInvoice.setActive(false);
        deletedInvoice.setDeleted(true);

        when(invoiceRepository.findById("inv-deleted")).thenReturn(Optional.of(deletedInvoice));

        com.viraj.dmabackend.invoice.entity.InvoiceHistory history =
                new com.viraj.dmabackend.invoice.entity.InvoiceHistory();
        history.setInvoiceId("inv-deleted");
        history.setActive(false);

        when(invoiceHistoryRepository.findByInvoiceIdOrderByInvoiceVersionAsc("inv-deleted"))
                .thenReturn(List.of(history));

        var result = invoiceService.getInvoiceHistory("inv-deleted");
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void updateInvoiceStatus_ThrowsWhenStatusIsSameAsCurrent() {
        Invoice invoice = new Invoice();
        invoice.setId("inv-123");
        invoice.setStatus(InvoiceStatus.SENT);
        invoice.setActive(true);

        when(invoiceRepository.findByIdAndActiveTrue("inv-123")).thenReturn(Optional.of(invoice));

        com.viraj.dmabackend.invoice.dto.UpdateInvoiceStatusRequest req =
                new com.viraj.dmabackend.invoice.dto.UpdateInvoiceStatusRequest();
        req.setStatus(InvoiceStatus.SENT);

        assertThrows(InvalidInvoiceStatusException.class,
                () -> invoiceService.updateInvoiceStatus("inv-123", req));
    }

    @Test
    void updateInvoiceStatus_ThrowsWhenRevertingSentToDraft() {
        Invoice invoice = new Invoice();
        invoice.setId("inv-123");
        invoice.setStatus(InvoiceStatus.SENT);
        invoice.setActive(true);

        when(invoiceRepository.findByIdAndActiveTrue("inv-123")).thenReturn(Optional.of(invoice));

        com.viraj.dmabackend.invoice.dto.UpdateInvoiceStatusRequest req =
                new com.viraj.dmabackend.invoice.dto.UpdateInvoiceStatusRequest();
        req.setStatus(InvoiceStatus.DRAFT);

        assertThrows(InvalidInvoiceStatusException.class,
                () -> invoiceService.updateInvoiceStatus("inv-123", req));
    }

    @Test
    void updateInvoiceStatus_ThrowsWhenTransitioningDraftToOverdue() {
        Invoice invoice = new Invoice();
        invoice.setId("inv-123");
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setActive(true);

        when(invoiceRepository.findByIdAndActiveTrue("inv-123")).thenReturn(Optional.of(invoice));

        com.viraj.dmabackend.invoice.dto.UpdateInvoiceStatusRequest req =
                new com.viraj.dmabackend.invoice.dto.UpdateInvoiceStatusRequest();
        req.setStatus(InvoiceStatus.OVERDUE);

        assertThrows(InvalidInvoiceStatusException.class,
                () -> invoiceService.updateInvoiceStatus("inv-123", req));
    }

    @Test
    void updateInvoiceStatus_ThrowsWhenRevertingOverdueToSent() {
        Invoice invoice = new Invoice();
        invoice.setId("inv-123");
        invoice.setStatus(InvoiceStatus.OVERDUE);
        invoice.setActive(true);

        when(invoiceRepository.findByIdAndActiveTrue("inv-123")).thenReturn(Optional.of(invoice));

        com.viraj.dmabackend.invoice.dto.UpdateInvoiceStatusRequest req =
                new com.viraj.dmabackend.invoice.dto.UpdateInvoiceStatusRequest();
        req.setStatus(InvoiceStatus.SENT);

        assertThrows(InvalidInvoiceStatusException.class,
                () -> invoiceService.updateInvoiceStatus("inv-123", req));
    }

    @Test
    void deleteInvoice_ThrowsWhenStatusIsSentOrOverdue() {
        Invoice sentInvoice = new Invoice();
        sentInvoice.setId("inv-sent");
        sentInvoice.setStatus(InvoiceStatus.SENT);
        sentInvoice.setActive(true);

        when(invoiceRepository.findByIdAndActiveTrue("inv-sent")).thenReturn(Optional.of(sentInvoice));

        assertThrows(InvalidInvoiceStatusException.class,
                () -> invoiceService.deleteInvoice("inv-sent"));

        Invoice overdueInvoice = new Invoice();
        overdueInvoice.setId("inv-overdue");
        overdueInvoice.setStatus(InvoiceStatus.OVERDUE);
        overdueInvoice.setActive(true);

        when(invoiceRepository.findByIdAndActiveTrue("inv-overdue")).thenReturn(Optional.of(overdueInvoice));

        assertThrows(InvalidInvoiceStatusException.class,
                () -> invoiceService.deleteInvoice("inv-overdue"));

        Invoice cancelledInvoice = new Invoice();
        cancelledInvoice.setId("inv-cancelled");
        cancelledInvoice.setStatus(InvoiceStatus.CANCELLED);
        cancelledInvoice.setActive(true);

        when(invoiceRepository.findByIdAndActiveTrue("inv-cancelled")).thenReturn(Optional.of(cancelledInvoice));

        assertThrows(InvalidInvoiceStatusException.class,
                () -> invoiceService.deleteInvoice("inv-cancelled"));

        Invoice paidInvoice = new Invoice();
        paidInvoice.setId("inv-paid");
        paidInvoice.setStatus(InvoiceStatus.PAID);
        paidInvoice.setActive(true);

        when(invoiceRepository.findByIdAndActiveTrue("inv-paid")).thenReturn(Optional.of(paidInvoice));

        assertThrows(InvalidInvoiceStatusException.class,
                () -> invoiceService.deleteInvoice("inv-paid"));
    }

    @Test
    void updateInvoiceStatus_ThrowsWhenVersionMismatch() {
        Invoice invoice = new Invoice();
        invoice.setId("inv-123");
        invoice.setStatus(InvoiceStatus.SENT);
        invoice.setVersion(2L);
        invoice.setActive(true);

        when(invoiceRepository.findByIdAndActiveTrue("inv-123")).thenReturn(Optional.of(invoice));

        com.viraj.dmabackend.invoice.dto.UpdateInvoiceStatusRequest req =
                new com.viraj.dmabackend.invoice.dto.UpdateInvoiceStatusRequest();
        req.setStatus(InvoiceStatus.PAID);
        req.setVersion(1L);

        assertThrows(org.springframework.dao.OptimisticLockingFailureException.class,
                () -> invoiceService.updateInvoiceStatus("inv-123", req));
    }

    @Test
    void updateInvoiceStatus_ThrowsWhenTransitioningSentToOverdueBeforeDueDate() {
        Invoice invoice = new Invoice();
        invoice.setId("inv-123");
        invoice.setStatus(InvoiceStatus.SENT);
        invoice.setDueDate(LocalDate.now(ZoneOffset.UTC).plusDays(5));
        invoice.setActive(true);

        when(invoiceRepository.findByIdAndActiveTrue("inv-123")).thenReturn(Optional.of(invoice));

        com.viraj.dmabackend.invoice.dto.UpdateInvoiceStatusRequest req =
                new com.viraj.dmabackend.invoice.dto.UpdateInvoiceStatusRequest();
        req.setStatus(InvoiceStatus.OVERDUE);

        assertThrows(InvalidInvoiceStatusException.class,
                () -> invoiceService.updateInvoiceStatus("inv-123", req));
    }

    @Test
    void updateInvoiceStatus_SucceedsWhenTransitioningSentToOverdueAfterDueDate() {
        Invoice invoice = new Invoice();
        invoice.setId("inv-123");
        invoice.setStatus(InvoiceStatus.SENT);
        invoice.setDueDate(LocalDate.now(ZoneOffset.UTC).minusDays(1));
        invoice.setActive(true);

        when(invoiceRepository.findByIdAndActiveTrue("inv-123")).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);
        when(invoiceMapper.toResponse(any())).thenReturn(InvoiceResponse.builder().id("inv-123").status(InvoiceStatus.OVERDUE).build());

        com.viraj.dmabackend.invoice.dto.UpdateInvoiceStatusRequest req =
                new com.viraj.dmabackend.invoice.dto.UpdateInvoiceStatusRequest();
        req.setStatus(InvoiceStatus.OVERDUE);

        InvoiceResponse response = invoiceService.updateInvoiceStatus("inv-123", req);
        assertNotNull(response);
        assertEquals(InvoiceStatus.OVERDUE, response.getStatus());
    }

    @Test
    void deleteInvoice_SucceedsWhenStatusIsDraft() {
        Invoice draftInvoice = new Invoice();
        draftInvoice.setId("inv-draft");
        draftInvoice.setStatus(InvoiceStatus.DRAFT);
        draftInvoice.setActive(true);

        when(invoiceRepository.findByIdAndActiveTrue("inv-draft")).thenReturn(Optional.of(draftInvoice));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(draftInvoice);

        assertDoesNotThrow(() -> invoiceService.deleteInvoice("inv-draft"));
        verify(invoiceRepository).save(draftInvoice);
        assertFalse(draftInvoice.getActive());
        assertTrue(draftInvoice.isDeleted());
    }

    @Test
    void saveInvoiceHistory_defensivelyClonesInvoiceItems() {
        InvoiceItem item = InvoiceItem.builder()
                .description("Initial Desc")
                .quantity(new BigDecimal("1"))
                .unitPrice(new BigDecimal("100.00"))
                .lineTotal(new BigDecimal("100.00"))
                .build();

        Invoice invoice = Invoice.builder()
                .id("inv-clone")
                .invoiceNumber("INV-2026-0001")
                .clientId("client-1")
                .projectId("proj-1")
                .items(new java.util.ArrayList<>(List.of(item)))
                .status(InvoiceStatus.DRAFT)
                .active(true)
                .build();

        when(invoiceRepository.findByIdAndActiveTrue("inv-clone")).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);

        invoiceService.deleteInvoice("inv-clone");

        org.mockito.ArgumentCaptor<InvoiceHistory> captor = org.mockito.ArgumentCaptor.forClass(InvoiceHistory.class);
        verify(invoiceHistoryRepository).save(captor.capture());
        InvoiceHistory savedHistory = captor.getValue();

        // Mutate original item
        item.setDescription("Mutated Desc");

        // Verify history retains cloned snapshot
        assertEquals("Initial Desc", savedHistory.getItems().get(0).getDescription());
    }

    @Test
    void saveInvoiceHistory_DefaultsInvoiceVersionToZeroWhenNull() {
        Invoice invoice = Invoice.builder()
                .id("inv-null-ver")
                .invoiceNumber("INV-2026-999")
                .status(InvoiceStatus.DRAFT)
                .active(true)
                .build();
        invoice.setVersion(null);

        when(invoiceRepository.findByIdAndActiveTrue("inv-null-ver")).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);

        invoiceService.deleteInvoice("inv-null-ver");

        org.mockito.ArgumentCaptor<InvoiceHistory> captor = org.mockito.ArgumentCaptor.forClass(InvoiceHistory.class);
        verify(invoiceHistoryRepository).save(captor.capture());
        InvoiceHistory savedHistory = captor.getValue();

        assertEquals(0L, savedHistory.getInvoiceVersion());
    }

    @Test
    void saveInvoiceHistory_CapturesUsernameWhenPrincipalIsUserDetails() {
        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User(
                        "accountant@example.com",
                        "password",
                        org.springframework.security.core.authority.AuthorityUtils.createAuthorityList("ROLE_USER")
                );

        org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        userDetails,
                        "credentials",
                        userDetails.getAuthorities()
                );
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

        Invoice invoice = Invoice.builder()
                .id("inv-user-audit")
                .invoiceNumber("INV-2026-0002")
                .clientId("client-1")
                .projectId("proj-1")
                .status(InvoiceStatus.DRAFT)
                .active(true)
                .build();

        when(invoiceRepository.findByIdAndActiveTrue("inv-user-audit")).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);

        invoiceService.deleteInvoice("inv-user-audit");

        org.mockito.ArgumentCaptor<InvoiceHistory> captor = org.mockito.ArgumentCaptor.forClass(InvoiceHistory.class);
        verify(invoiceHistoryRepository, atLeastOnce()).save(captor.capture());
        InvoiceHistory savedHistory = captor.getValue();

        assertEquals("accountant@example.com", savedHistory.getChangedBy());
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void getInvoicesByClientId_ValidatesClientAndReturnsPage() {
        String clientId = "client-123";
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        Invoice invoice = new Invoice();
        invoice.setId("inv-1");
        com.viraj.dmabackend.invoice.dto.InvoiceResponse response = new com.viraj.dmabackend.invoice.dto.InvoiceResponse();

        org.springframework.data.domain.Page<Invoice> page = new org.springframework.data.domain.PageImpl<>(List.of(invoice));
        when(invoiceRepository.findByActiveTrueAndClientId(clientId, pageable)).thenReturn(page);
        when(invoiceMapper.toResponse(invoice)).thenReturn(response);

        org.springframework.data.domain.Page<com.viraj.dmabackend.invoice.dto.InvoiceResponse> result =
                invoiceService.getInvoicesByClientId("  client-123  ", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(invoiceValidator).validateClientExists(clientId);
        verify(invoiceRepository).findByActiveTrueAndClientId(clientId, pageable);
    }

    @Test
    void getInvoicesByProjectId_ValidatesProjectAndReturnsPage() {
        String projectId = "proj-123";
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        Invoice invoice = new Invoice();
        invoice.setId("inv-2");
        com.viraj.dmabackend.invoice.dto.InvoiceResponse response = new com.viraj.dmabackend.invoice.dto.InvoiceResponse();

        org.springframework.data.domain.Page<Invoice> page = new org.springframework.data.domain.PageImpl<>(List.of(invoice));
        when(invoiceRepository.findByActiveTrueAndProjectId(projectId, pageable)).thenReturn(page);
        when(invoiceMapper.toResponse(invoice)).thenReturn(response);

        org.springframework.data.domain.Page<com.viraj.dmabackend.invoice.dto.InvoiceResponse> result =
                invoiceService.getInvoicesByProjectId("  proj-123  ", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(invoiceValidator).validateProjectExists(projectId);
        verify(invoiceRepository).findByActiveTrueAndProjectId(projectId, pageable);
    }

    @Test
    void updateInvoice_ThrowsWhenStatusIsNotDraft() {
        for (InvoiceStatus nonDraftStatus : List.of(InvoiceStatus.SENT, InvoiceStatus.OVERDUE, InvoiceStatus.PAID, InvoiceStatus.CANCELLED)) {
            Invoice invoice = new Invoice();
            invoice.setId("inv-" + nonDraftStatus);
            invoice.setStatus(nonDraftStatus);
            invoice.setActive(true);

            when(invoiceRepository.findByIdAndActiveTrue("inv-" + nonDraftStatus)).thenReturn(Optional.of(invoice));

            com.viraj.dmabackend.invoice.dto.UpdateInvoiceRequest request =
                    new com.viraj.dmabackend.invoice.dto.UpdateInvoiceRequest();

            assertThrows(InvalidInvoiceStatusException.class,
                    () -> invoiceService.updateInvoice("inv-" + nonDraftStatus, request));
        }
    }

    @Test
    void updateInvoice_SucceedsWhenStatusIsDraft() {
        Invoice invoice = new Invoice();
        invoice.setId("inv-draft");
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setActive(true);
        invoice.setVersion(2L);

        when(invoiceRepository.findByIdAndActiveTrue("inv-draft")).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(invoiceMapper.toResponse(any())).thenReturn(new com.viraj.dmabackend.invoice.dto.InvoiceResponse());

        com.viraj.dmabackend.invoice.dto.UpdateInvoiceRequest request =
                new com.viraj.dmabackend.invoice.dto.UpdateInvoiceRequest();
        request.setDueDate(java.time.LocalDate.now().plusDays(10));
        request.setTaxPercentage(BigDecimal.TEN);
        request.setDiscount(BigDecimal.ZERO);
        request.setVersion(2L);

        assertDoesNotThrow(() -> invoiceService.updateInvoice("inv-draft", request));
        verify(invoiceValidator).validateUpdateInvoice(invoice, request);
        verify(invoiceRepository).save(invoice);
    }

    @Test
    void updateInvoice_ThrowsOptimisticLockingFailure_WhenVersionMismatch() {
        Invoice invoice = new Invoice();
        invoice.setId("inv-draft");
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setActive(true);
        invoice.setVersion(2L);

        when(invoiceRepository.findByIdAndActiveTrue("inv-draft")).thenReturn(Optional.of(invoice));

        com.viraj.dmabackend.invoice.dto.UpdateInvoiceRequest request =
                new com.viraj.dmabackend.invoice.dto.UpdateInvoiceRequest();
        request.setVersion(1L); // Stale version from concurrent editor

        assertThrows(org.springframework.dao.OptimisticLockingFailureException.class,
                () -> invoiceService.updateInvoice("inv-draft", request));
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void getInvoiceById_TrimsInvoiceId() {
        Invoice invoice = new Invoice();
        invoice.setId("inv-1");
        when(invoiceRepository.findByIdAndActiveTrue("inv-1")).thenReturn(Optional.of(invoice));
        when(invoiceMapper.toResponse(invoice)).thenReturn(new com.viraj.dmabackend.invoice.dto.InvoiceResponse());

        com.viraj.dmabackend.invoice.dto.InvoiceResponse response = invoiceService.getInvoiceById("   inv-1   ");

        assertNotNull(response);
        verify(invoiceRepository).findByIdAndActiveTrue("inv-1");
    }

    @Test
    void getAllInvoices_ClampsOversizedPageable() {
        org.springframework.data.domain.PageRequest oversized = org.springframework.data.domain.PageRequest.of(0, 1000);
        when(invoiceRepository.findByActiveTrue(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(org.springframework.data.domain.Page.empty());

        invoiceService.getAllInvoices(oversized);

        org.mockito.ArgumentCaptor<org.springframework.data.domain.Pageable> captor =
                org.mockito.ArgumentCaptor.forClass(org.springframework.data.domain.Pageable.class);
        verify(invoiceRepository).findByActiveTrue(captor.capture());
        assertEquals(100, captor.getValue().getPageSize());
    }
}
