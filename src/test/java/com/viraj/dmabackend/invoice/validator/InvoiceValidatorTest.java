package com.viraj.dmabackend.invoice.validator;

import com.viraj.dmabackend.client.dto.ClientResponse;
import com.viraj.dmabackend.client.enums.ClientStatus;
import com.viraj.dmabackend.client.service.ClientService;
import com.viraj.dmabackend.exception.BadRequestException;
import com.viraj.dmabackend.invoice.dto.CreateInvoiceRequest;
import com.viraj.dmabackend.project.dto.ProjectResponse;
import com.viraj.dmabackend.project.enums.ProjectStatus;
import com.viraj.dmabackend.project.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import com.viraj.dmabackend.client.exception.ClientNotFoundException;
import com.viraj.dmabackend.client.repository.ClientRepository;
import com.viraj.dmabackend.project.exception.ProjectNotFoundException;
import com.viraj.dmabackend.project.repository.ProjectRepository;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceValidatorTest {

    @Mock
    private ClientService clientService;

    @Mock
    private ProjectService projectService;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private InvoiceValidator invoiceValidator;

    @Test
    void validateCreateInvoice_whenClientIsInactive_throwsBadRequestException() {
        ClientResponse inactiveClient = ClientResponse.builder().id("client-1").status(ClientStatus.INACTIVE).build();
        when(clientService.getClientById("client-1")).thenReturn(inactiveClient);

        CreateInvoiceRequest request = CreateInvoiceRequest.builder()
                .clientId("client-1")
                .projectId("proj-1")
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(10))
                .items(Collections.emptyList())
                .discount(BigDecimal.ZERO)
                .build();

        assertThatThrownBy(() -> invoiceValidator.validateCreateInvoice(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot create an invoice for an inactive client");
    }

    @Test
    void validateCreateInvoice_whenProjectIsCancelled_throwsBadRequestException() {
        ClientResponse activeClient = ClientResponse.builder().id("client-1").status(ClientStatus.ACTIVE).build();
        ProjectResponse cancelledProject = ProjectResponse.builder().id("proj-1").clientId("client-1").status(ProjectStatus.CANCELLED).build();

        when(clientService.getClientById("client-1")).thenReturn(activeClient);
        when(projectService.getProjectById("proj-1")).thenReturn(cancelledProject);

        CreateInvoiceRequest request = CreateInvoiceRequest.builder()
                .clientId("client-1")
                .projectId("proj-1")
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(10))
                .items(Collections.emptyList())
                .discount(BigDecimal.ZERO)
                .build();

        assertThatThrownBy(() -> invoiceValidator.validateCreateInvoice(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot create an invoice for a cancelled project");
    }

    @Test
    void validateCreateInvoice_whenProjectIsArchived_throwsBadRequestException() {
        ClientResponse activeClient = ClientResponse.builder().id("client-1").status(ClientStatus.ACTIVE).build();
        ProjectResponse archivedProject = ProjectResponse.builder().id("proj-1").clientId("client-1").status(ProjectStatus.ARCHIVED).build();

        when(clientService.getClientById("client-1")).thenReturn(activeClient);
        when(projectService.getProjectById("proj-1")).thenReturn(archivedProject);

        CreateInvoiceRequest request = CreateInvoiceRequest.builder()
                .clientId("client-1")
                .projectId("proj-1")
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(10))
                .items(Collections.emptyList())
                .discount(BigDecimal.ZERO)
                .build();

        assertThatThrownBy(() -> invoiceValidator.validateCreateInvoice(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot create an invoice for an archived project");
    }

    @Test
    void validateCreateInvoice_whenClientAndProjectAreValid_passesValidation() {
        ClientResponse activeClient = ClientResponse.builder().id("client-1").status(ClientStatus.ACTIVE).build();
        ProjectResponse activeProject = ProjectResponse.builder().id("proj-1").clientId("client-1").status(ProjectStatus.ACTIVE).build();

        when(clientService.getClientById("client-1")).thenReturn(activeClient);
        when(projectService.getProjectById("proj-1")).thenReturn(activeProject);

        CreateInvoiceRequest request = CreateInvoiceRequest.builder()
                .clientId("client-1")
                .projectId("proj-1")
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(10))
                .items(Collections.emptyList())
                .discount(BigDecimal.ZERO)
                .build();

        assertThatCode(() -> invoiceValidator.validateCreateInvoice(request))
                .doesNotThrowAnyException();
    }

    @Test
    void validateCreateInvoice_whenClientIdAndProjectIdHaveWhitespace_trimsBeforeServiceCalls() {
        ClientResponse activeClient = ClientResponse.builder().id("client-1").status(ClientStatus.ACTIVE).build();
        ProjectResponse activeProject = ProjectResponse.builder().id("proj-1").clientId("client-1").status(ProjectStatus.ACTIVE).build();

        when(clientService.getClientById("client-1")).thenReturn(activeClient);
        when(projectService.getProjectById("proj-1")).thenReturn(activeProject);

        CreateInvoiceRequest request = CreateInvoiceRequest.builder()
                .clientId("  client-1  ")
                .projectId("  proj-1  ")
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(10))
                .items(Collections.emptyList())
                .discount(BigDecimal.ZERO)
                .build();

        assertThatCode(() -> invoiceValidator.validateCreateInvoice(request))
                .doesNotThrowAnyException();
    }

    @Test
    void validateClientExists_checksClientRepository() {
        when(clientRepository.existsById("client-123")).thenReturn(true);
        assertThatCode(() -> invoiceValidator.validateClientExists("client-123")).doesNotThrowAnyException();
    }

    @Test
    void validateClientExists_whenNotFound_throwsClientNotFoundException() {
        when(clientRepository.existsById("client-missing")).thenReturn(false);
        assertThatThrownBy(() -> invoiceValidator.validateClientExists("client-missing"))
                .isInstanceOf(ClientNotFoundException.class);
    }

    @Test
    void validateProjectExists_checksProjectRepository() {
        when(projectRepository.existsById("proj-123")).thenReturn(true);
        assertThatCode(() -> invoiceValidator.validateProjectExists("proj-123")).doesNotThrowAnyException();
    }

    @Test
    void validateProjectExists_whenNotFound_throwsProjectNotFoundException() {
        when(projectRepository.existsById("proj-missing")).thenReturn(false);
        assertThatThrownBy(() -> invoiceValidator.validateProjectExists("proj-missing"))
                .isInstanceOf(ProjectNotFoundException.class);
    }

    @Test
    void validateUpdateInvoice_whenClientIsInactive_throwsBadRequestException() {
        ClientResponse inactiveClient = ClientResponse.builder().id("client-1").status(ClientStatus.INACTIVE).build();
        when(clientService.getClientById("client-1")).thenReturn(inactiveClient);

        com.viraj.dmabackend.invoice.entity.Invoice invoice = com.viraj.dmabackend.invoice.entity.Invoice.builder()
                .clientId("client-1")
                .projectId("proj-1")
                .issueDate(LocalDate.now())
                .build();

        com.viraj.dmabackend.invoice.dto.UpdateInvoiceRequest request = com.viraj.dmabackend.invoice.dto.UpdateInvoiceRequest.builder()
                .dueDate(LocalDate.now().plusDays(5))
                .items(Collections.emptyList())
                .discount(BigDecimal.ZERO)
                .build();

        assertThatThrownBy(() -> invoiceValidator.validateUpdateInvoice(invoice, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot create an invoice for an inactive client");
    }

    @Test
    void validateUpdateInvoice_whenProjectIsCancelled_throwsBadRequestException() {
        ClientResponse activeClient = ClientResponse.builder().id("client-1").status(ClientStatus.ACTIVE).build();
        ProjectResponse cancelledProject = ProjectResponse.builder().id("proj-1").clientId("client-1").status(ProjectStatus.CANCELLED).build();

        when(clientService.getClientById("client-1")).thenReturn(activeClient);
        when(projectService.getProjectById("proj-1")).thenReturn(cancelledProject);

        com.viraj.dmabackend.invoice.entity.Invoice invoice = com.viraj.dmabackend.invoice.entity.Invoice.builder()
                .clientId("client-1")
                .projectId("proj-1")
                .issueDate(LocalDate.now())
                .build();

        com.viraj.dmabackend.invoice.dto.UpdateInvoiceRequest request = com.viraj.dmabackend.invoice.dto.UpdateInvoiceRequest.builder()
                .dueDate(LocalDate.now().plusDays(5))
                .items(Collections.emptyList())
                .discount(BigDecimal.ZERO)
                .build();

        assertThatThrownBy(() -> invoiceValidator.validateUpdateInvoice(invoice, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot create an invoice for a cancelled project");
    }

    @Test
    void validateCreateInvoice_whenDiscountExceedsRoundedSubtotal_throwsBadRequestException() {
        ClientResponse activeClient = ClientResponse.builder().id("client-1").status(ClientStatus.ACTIVE).build();
        ProjectResponse activeProject = ProjectResponse.builder().id("proj-1").clientId("client-1").status(ProjectStatus.ACTIVE).build();

        when(clientService.getClientById("client-1")).thenReturn(activeClient);
        when(projectService.getProjectById("proj-1")).thenReturn(activeProject);

        com.viraj.dmabackend.invoice.dto.InvoiceItemRequest item = com.viraj.dmabackend.invoice.dto.InvoiceItemRequest.builder()
                .description("Item 1")
                .quantity(new BigDecimal("1"))
                .unitPrice(new BigDecimal("10.004")) // rounds to 10.00
                .build();

        CreateInvoiceRequest request = CreateInvoiceRequest.builder()
                .clientId("client-1")
                .projectId("proj-1")
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(10))
                .items(java.util.List.of(item))
                .discount(new BigDecimal("10.01")) // exceeds rounded 10.00
                .build();

        assertThatThrownBy(() -> invoiceValidator.validateCreateInvoice(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Discount cannot be greater than subtotal");
    }

    @Test
    void validateCreateInvoice_whenDiscountEqualsRoundedSubtotal_passesValidation() {
        ClientResponse activeClient = ClientResponse.builder().id("client-1").status(ClientStatus.ACTIVE).build();
        ProjectResponse activeProject = ProjectResponse.builder().id("proj-1").clientId("client-1").status(ProjectStatus.ACTIVE).build();

        when(clientService.getClientById("client-1")).thenReturn(activeClient);
        when(projectService.getProjectById("proj-1")).thenReturn(activeProject);

        com.viraj.dmabackend.invoice.dto.InvoiceItemRequest item = com.viraj.dmabackend.invoice.dto.InvoiceItemRequest.builder()
                .description("Item 1")
                .quantity(new BigDecimal("1"))
                .unitPrice(new BigDecimal("10.006")) // rounds to 10.01
                .build();

        CreateInvoiceRequest request = CreateInvoiceRequest.builder()
                .clientId("client-1")
                .projectId("proj-1")
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(10))
                .items(java.util.List.of(item))
                .discount(new BigDecimal("10.01")) // exactly equals rounded 10.01
                .build();

        assertThatCode(() -> invoiceValidator.validateCreateInvoice(request))
                .doesNotThrowAnyException();
    }
}
