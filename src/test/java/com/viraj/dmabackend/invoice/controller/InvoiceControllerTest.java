package com.viraj.dmabackend.invoice.controller;

import com.viraj.dmabackend.controller.BaseControllerWebMvcTest;
import com.viraj.dmabackend.invoice.dto.*;
import com.viraj.dmabackend.invoice.enums.InvoiceStatus;
import com.viraj.dmabackend.invoice.service.InvoiceService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InvoiceController.class)
class InvoiceControllerTest extends BaseControllerWebMvcTest {

    @MockBean
    private InvoiceService invoiceService;

    @Test
    @WithMockUser(authorities = "invoice:create")
    void createInvoice_whenAuthorized_returnsCreated() throws Exception {
        CreateInvoiceRequest request = CreateInvoiceRequest.builder()
                .clientId("client-1")
                .projectId("proj-1")
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(15))
                .taxPercentage(BigDecimal.valueOf(18.0))
                .discount(BigDecimal.ZERO)
                .items(Collections.singletonList(InvoiceItemRequest.builder()
                        .description("SEO Service")
                        .quantity(BigDecimal.ONE)
                        .unitPrice(BigDecimal.valueOf(1000))
                        .build()))
                .build();

        when(invoiceService.createInvoice(any())).thenReturn(InvoiceResponse.builder().id("inv-1").build());

        mockMvc.perform(post("/api/v1/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(authorities = "invoice:read")
    void createInvoice_whenInsufficientAuthority_returnsForbidden() throws Exception {
        CreateInvoiceRequest request = CreateInvoiceRequest.builder()
                .clientId("client-1")
                .projectId("proj-1")
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(15))
                .taxPercentage(BigDecimal.valueOf(18.0))
                .discount(BigDecimal.ZERO)
                .items(Collections.singletonList(InvoiceItemRequest.builder()
                        .description("SEO Service")
                        .quantity(BigDecimal.ONE)
                        .unitPrice(BigDecimal.valueOf(1000))
                        .build()))
                .build();

        mockMvc.perform(post("/api/v1/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createInvoice_whenUnauthenticated_returnsUnauthorized() throws Exception {
        CreateInvoiceRequest request = CreateInvoiceRequest.builder()
                .clientId("client-1")
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(15))
                .build();

        mockMvc.perform(post("/api/v1/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "invoice:read")
    void getInvoiceById_whenAuthorized_returnsOk() throws Exception {
        when(invoiceService.getInvoiceById("inv-1")).thenReturn(InvoiceResponse.builder().id("inv-1").build());

        mockMvc.perform(get("/api/v1/invoices/inv-1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "invoice:read")
    void getInvoiceHistory_whenAuthorized_returnsOk() throws Exception {
        when(invoiceService.getInvoiceHistory("inv-1")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/invoices/inv-1/history"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "invoice:read")
    void getAllInvoices_whenAuthorized_returnsOk() throws Exception {
        when(invoiceService.getAllInvoices(any())).thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/v1/invoices"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "invoice:read")
    void getInvoicesByStatus_whenAuthorized_returnsOk() throws Exception {
        when(invoiceService.getInvoicesByStatus(eq(InvoiceStatus.DRAFT), any())).thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/v1/invoices/status/DRAFT"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "invoice:read")
    void getInvoicesByClientId_whenAuthorized_returnsOk() throws Exception {
        when(invoiceService.getInvoicesByClientId(eq("client-1"), any())).thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/v1/invoices/client/client-1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "invoice:read")
    void getInvoicesByProjectId_whenAuthorized_returnsOk() throws Exception {
        when(invoiceService.getInvoicesByProjectId(eq("proj-1"), any())).thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/v1/invoices/project/proj-1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "invoice:update")
    void updateInvoice_whenAuthorized_returnsOk() throws Exception {
        UpdateInvoiceRequest request = UpdateInvoiceRequest.builder()
                .version(1L)
                .dueDate(LocalDate.now().plusDays(30))
                .taxPercentage(BigDecimal.valueOf(18.0))
                .discount(BigDecimal.ZERO)
                .items(Collections.singletonList(InvoiceItemRequest.builder()
                        .description("SEO Service")
                        .quantity(BigDecimal.ONE)
                        .unitPrice(BigDecimal.valueOf(1000))
                        .build()))
                .build();

        when(invoiceService.updateInvoice(eq("inv-1"), any())).thenReturn(InvoiceResponse.builder().id("inv-1").build());

        mockMvc.perform(put("/api/v1/invoices/inv-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "invoice:update")
    void updateInvoiceStatus_whenAuthorized_returnsOk() throws Exception {
        UpdateInvoiceStatusRequest request = new UpdateInvoiceStatusRequest();
        request.setStatus(InvoiceStatus.SENT);

        when(invoiceService.updateInvoiceStatus(eq("inv-1"), any())).thenReturn(InvoiceResponse.builder().id("inv-1").build());

        mockMvc.perform(patch("/api/v1/invoices/inv-1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "invoice:delete")
    void deleteInvoice_whenAuthorized_returnsNoContent() throws Exception {
        doNothing().when(invoiceService).deleteInvoice("inv-1");

        mockMvc.perform(delete("/api/v1/invoices/inv-1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = "invoice:read")
    void deleteInvoice_whenInsufficientAuthority_returnsForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/invoices/inv-1"))
                .andExpect(status().isForbidden());
    }
}
