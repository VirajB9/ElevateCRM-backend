package com.viraj.dmabackend.integration;

import com.viraj.dmabackend.auth.entity.User;
import com.viraj.dmabackend.auth.enums.UserStatus;
import com.viraj.dmabackend.auth.repository.UserRepository;
import com.viraj.dmabackend.client.entity.Client;
import com.viraj.dmabackend.client.enums.ClientStatus;
import com.viraj.dmabackend.client.repository.ClientRepository;
import com.viraj.dmabackend.lead.entity.Lead;
import com.viraj.dmabackend.lead.enums.LeadSource;
import com.viraj.dmabackend.lead.enums.LeadStatus;
import com.viraj.dmabackend.lead.repository.LeadRepository;
import com.viraj.dmabackend.project.entity.Project;
import com.viraj.dmabackend.project.enums.ProjectStatus;
import com.viraj.dmabackend.project.repository.ProjectRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class MongoSearchRepositoryIntegrationTest extends AbstractMongoIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
        clientRepository.deleteAll();
        leadRepository.deleteAll();
        projectRepository.deleteAll();
    }

    @Test
    void userSearch_findsUsersByFirstNameOrLastNameOrEmail() {
        User user1 = User.builder()
                .firstName("Alexander")
                .lastName("Hamilton")
                .email("alexander@treasury.gov")
                .phoneNumber("9876543101")
                .password("pwd")
                .roleId("role-1")
                .status(UserStatus.ACTIVE)
                .build();
        user1.setDeleted(false);

        User user2 = User.builder()
                .firstName("Thomas")
                .lastName("Jefferson")
                .email("thomas@state.gov")
                .phoneNumber("9876543102")
                .password("pwd")
                .roleId("role-1")
                .status(UserStatus.ACTIVE)
                .build();
        user2.setDeleted(false);

        userRepository.save(user1);
        userRepository.save(user2);

        Page<User> results = userRepository.searchUsers("Alexander", PageRequest.of(0, 10));
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getFirstName()).isEqualTo("Alexander");
    }

    @Test
    void clientSearch_findsClientsByCompanyNameOrContactPerson() {
        Client client1 = Client.builder()
                .companyName("Stark Industries")
                .contactPerson("Tony Stark")
                .email("tony@stark.com")
                .phoneNumber("9876543201")
                .status(ClientStatus.ACTIVE)
                .build();
        client1.setDeleted(false);

        Client client2 = Client.builder()
                .companyName("Wayne Enterprises")
                .contactPerson("Bruce Wayne")
                .email("bruce@wayne.com")
                .phoneNumber("9876543202")
                .status(ClientStatus.ACTIVE)
                .build();
        client2.setDeleted(false);

        clientRepository.save(client1);
        clientRepository.save(client2);

        Page<Client> results = clientRepository.searchClients("industries", PageRequest.of(0, 10));
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getCompanyName()).isEqualTo("Stark Industries");
    }

    @Test
    void leadSearch_findsLeadsByContactOrEmail() {
        Lead lead1 = Lead.builder()
                .firstName("Peter")
                .lastName("Parker")
                .companyName("Daily Bugle")
                .email("peter@bugle.com")
                .phoneNumber("9876543203")
                .status(LeadStatus.NEW)
                .source(LeadSource.WEBSITE)
                .build();
        lead1.setDeleted(false);

        leadRepository.save(lead1);

        Page<Lead> results = leadRepository.searchLeads("parker", PageRequest.of(0, 10));
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getLastName()).isEqualTo("Parker");
    }

    @Test
    void projectSearch_findsProjectsByName() {
        Project proj = Project.builder()
                .projectName("Quantum Computing Prototype")
                .clientId("client-99")
                .status(ProjectStatus.ACTIVE)
                .startDate(LocalDate.now())
                .build();
        proj.setDeleted(false);

        projectRepository.save(proj);

        Page<Project> results = projectRepository.searchProjects("quantum", PageRequest.of(0, 10));
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getProjectName()).isEqualTo("Quantum Computing Prototype");
    }
}
