package com.viraj.dmabackend.integration;

import com.viraj.dmabackend.auth.entity.User;
import com.viraj.dmabackend.auth.enums.UserStatus;
import com.viraj.dmabackend.auth.repository.UserRepository;
import com.viraj.dmabackend.client.entity.Client;
import com.viraj.dmabackend.client.enums.ClientStatus;
import com.viraj.dmabackend.client.repository.ClientRepository;
import com.viraj.dmabackend.invoice.entity.Invoice;
import com.viraj.dmabackend.invoice.enums.InvoiceStatus;
import com.viraj.dmabackend.invoice.repository.InvoiceRepository;
import com.viraj.dmabackend.project.entity.Project;
import com.viraj.dmabackend.project.enums.ProjectStatus;
import com.viraj.dmabackend.project.repository.ProjectRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MongoIndexIntegrationTest extends AbstractMongoIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
        clientRepository.deleteAll();
        projectRepository.deleteAll();
        invoiceRepository.deleteAll();
    }

    @Test
    void userEmail_uniqueIndex_enforcesUniquenessForActiveUsers() {
        User user1 = User.builder()
                .email("test.unique@example.com")
                .password("hashedpwd")
                .firstName("John")
                .lastName("Doe")
                .roleId("role-1")
                .status(UserStatus.ACTIVE)
                .build();
        user1.setDeleted(false);
        userRepository.save(user1);

        User user2 = User.builder()
                .email("test.unique@example.com")
                .password("hashedpwd2")
                .firstName("Jane")
                .lastName("Doe")
                .roleId("role-1")
                .status(UserStatus.ACTIVE)
                .build();
        user2.setDeleted(false);

        assertThatThrownBy(() -> userRepository.save(user2))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void userEmail_partialFilterIndex_allowsDuplicateEmailWhenPreviousUserDeleted() {
        User deletedUser = User.builder()
                .email("reuse.email@example.com")
                .password("hashedpwd")
                .firstName("Former")
                .lastName("Employee")
                .roleId("role-1")
                .status(UserStatus.DELETED)
                .build();
        deletedUser.setDeleted(true);
        userRepository.save(deletedUser);

        User activeUser = User.builder()
                .email("reuse.email@example.com")
                .password("hashedpwd2")
                .firstName("New")
                .lastName("Employee")
                .roleId("role-1")
                .status(UserStatus.ACTIVE)
                .build();
        activeUser.setDeleted(false);

        User saved = userRepository.save(activeUser);
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void clientGstAndEmail_uniqueIndexes_enforcesPartialFilterUniqueness() {
        Client client1 = Client.builder()
                .companyName("Acme Corp")
                .contactPerson("Alice")
                .email("contact@acme.com")
                .phoneNumber("9876543210")
                .gstNumber("27ABCDE1234F1Z5")
                .status(ClientStatus.ACTIVE)
                .build();
        client1.setDeleted(false);
        clientRepository.save(client1);

        Client duplicateGst = Client.builder()
                .companyName("Beta Corp")
                .contactPerson("Bob")
                .email("contact@beta.com")
                .phoneNumber("9876543211")
                .gstNumber("27ABCDE1234F1Z5")
                .status(ClientStatus.ACTIVE)
                .build();
        duplicateGst.setDeleted(false);

        assertThatThrownBy(() -> clientRepository.save(duplicateGst))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void project_compoundIndex_enforcesUniquenessOnClientAndProjectName() {
        Project proj1 = Project.builder()
                .projectName("Portal Revamp")
                .clientId("client-100")
                .status(ProjectStatus.ACTIVE)
                .startDate(LocalDate.now())
                .build();
        proj1.setDeleted(false);
        projectRepository.save(proj1);

        Project duplicateProjSameClient = Project.builder()
                .projectName("Portal Revamp")
                .clientId("client-100")
                .status(ProjectStatus.PLANNING)
                .startDate(LocalDate.now())
                .build();
        duplicateProjSameClient.setDeleted(false);

        assertThatThrownBy(() -> projectRepository.save(duplicateProjSameClient))
                .isInstanceOf(DuplicateKeyException.class);

        // Same project name for DIFFERENT client succeeds
        Project projDifferentClient = Project.builder()
                .projectName("Portal Revamp")
                .clientId("client-200")
                .status(ProjectStatus.PLANNING)
                .startDate(LocalDate.now())
                .build();
        projDifferentClient.setDeleted(false);

        Project saved = projectRepository.save(projDifferentClient);
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void invoice_invoiceNumber_uniqueIndexEnforced() {
        Invoice inv1 = Invoice.builder()
                .invoiceNumber("INV-TEST-001")
                .clientId("client-1")
                .status(InvoiceStatus.DRAFT)
                .active(true)
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(15))
                .totalAmount(BigDecimal.valueOf(1000))
                .build();
        invoiceRepository.save(inv1);

        Invoice inv2 = Invoice.builder()
                .invoiceNumber("INV-TEST-001")
                .clientId("client-2")
                .status(InvoiceStatus.DRAFT)
                .active(true)
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(15))
                .totalAmount(BigDecimal.valueOf(2000))
                .build();

        assertThatThrownBy(() -> invoiceRepository.save(inv2))
                .isInstanceOf(DuplicateKeyException.class);
    }
}
