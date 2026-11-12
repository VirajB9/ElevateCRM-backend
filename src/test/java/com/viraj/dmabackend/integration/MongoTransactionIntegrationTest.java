package com.viraj.dmabackend.integration;

import com.viraj.dmabackend.client.entity.Client;
import com.viraj.dmabackend.client.enums.ClientStatus;
import com.viraj.dmabackend.client.repository.ClientRepository;
import com.viraj.dmabackend.project.entity.Project;
import com.viraj.dmabackend.project.enums.ProjectStatus;
import com.viraj.dmabackend.project.repository.ProjectRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(MongoTransactionIntegrationTest.TestConfig.class)
class MongoTransactionIntegrationTest extends AbstractMongoIntegrationTest {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TransactionalTestService transactionalTestService;

    @AfterEach
    void cleanUp() {
        clientRepository.deleteAll();
        projectRepository.deleteAll();
    }

    @Test
    void multiDocumentTransaction_rollsBackAllWritesOnException() {
        Client client = Client.builder()
                .companyName("Rollback Company")
                .contactPerson("Alice")
                .email("rollback@example.com")
                .phoneNumber("9988776655")
                .status(ClientStatus.ACTIVE)
                .build();
        client.setDeleted(false);

        Project project = Project.builder()
                .projectName("Rollback Project")
                .clientId("temp-client-id")
                .status(ProjectStatus.PLANNING)
                .startDate(LocalDate.now())
                .build();
        project.setDeleted(false);

        assertThatThrownBy(() -> transactionalTestService.executeFailingMultiDocumentTransaction(client, project))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Simulated mid-transaction failure");

        // Assert neither document was committed to the database
        assertThat(clientRepository.findAll()).isEmpty();
        assertThat(projectRepository.findAll()).isEmpty();
    }

    @Test
    void multiDocumentTransaction_commitsAllWritesOnSuccess() {
        Client client = Client.builder()
                .companyName("Success Company")
                .contactPerson("Bob")
                .email("success@example.com")
                .phoneNumber("9988776644")
                .status(ClientStatus.ACTIVE)
                .build();
        client.setDeleted(false);

        Project project = Project.builder()
                .projectName("Success Project")
                .clientId("temp-client-id")
                .status(ProjectStatus.ACTIVE)
                .startDate(LocalDate.now())
                .build();
        project.setDeleted(false);

        transactionalTestService.executeSuccessfulMultiDocumentTransaction(client, project);

        assertThat(clientRepository.findAll()).hasSize(1);
        assertThat(projectRepository.findAll()).hasSize(1);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public TransactionalTestService transactionalTestService(ClientRepository clientRepo, ProjectRepository projectRepo) {
            return new TransactionalTestService(clientRepo, projectRepo);
        }
    }

    public static class TransactionalTestService {
        private final ClientRepository clientRepository;
        private final ProjectRepository projectRepository;

        public TransactionalTestService(ClientRepository clientRepository, ProjectRepository projectRepository) {
            this.clientRepository = clientRepository;
            this.projectRepository = projectRepository;
        }

        @Transactional
        public void executeFailingMultiDocumentTransaction(Client client, Project project) {
            clientRepository.save(client);
            projectRepository.save(project);
            throw new IllegalStateException("Simulated mid-transaction failure");
        }

        @Transactional
        public void executeSuccessfulMultiDocumentTransaction(Client client, Project project) {
            clientRepository.save(client);
            projectRepository.save(project);
        }
    }
}
