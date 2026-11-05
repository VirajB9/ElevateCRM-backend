package com.viraj.dmabackend.common.repository;

import com.viraj.dmabackend.auth.entity.User;
import com.viraj.dmabackend.auth.repository.UserRepositoryCustomImpl;
import com.viraj.dmabackend.client.entity.Client;
import com.viraj.dmabackend.client.repository.ClientRepositoryCustomImpl;
import com.viraj.dmabackend.lead.entity.Lead;
import com.viraj.dmabackend.lead.repository.LeadRepositoryCustomImpl;
import com.viraj.dmabackend.project.entity.Project;
import com.viraj.dmabackend.project.repository.ProjectRepositoryCustomImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class SearchRepositoriesTest {

    private MongoTemplate mongoTemplate;
    private UserRepositoryCustomImpl userRepositoryCustom;
    private ClientRepositoryCustomImpl clientRepositoryCustom;
    private LeadRepositoryCustomImpl leadRepositoryCustom;
    private ProjectRepositoryCustomImpl projectRepositoryCustom;

    private final Pageable pageable = PageRequest.of(0, 10);

    @BeforeEach
    void setUp() {
        mongoTemplate = mock(MongoTemplate.class);
        userRepositoryCustom = new UserRepositoryCustomImpl(mongoTemplate);
        clientRepositoryCustom = new ClientRepositoryCustomImpl(mongoTemplate);
        leadRepositoryCustom = new LeadRepositoryCustomImpl(mongoTemplate);
        projectRepositoryCustom = new ProjectRepositoryCustomImpl(mongoTemplate);
    }

    @Test
    void searchUsers_WithBlankKeyword_DoesNotUseTextQuery() {
        when(mongoTemplate.count(any(Query.class), eq(User.class))).thenReturn(0L);
        when(mongoTemplate.find(any(Query.class), eq(User.class))).thenReturn(Collections.emptyList());

        Page<User> result = userRepositoryCustom.searchUsers("   ", pageable);

        assertNotNull(result);
        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(captor.capture(), eq(User.class));
        assertFalse(captor.getValue().getQueryObject().containsKey("$text"));
    }

    @Test
    void searchClients_WithBlankKeyword_DoesNotUseTextQuery() {
        when(mongoTemplate.count(any(Query.class), eq(Client.class))).thenReturn(0L);
        when(mongoTemplate.find(any(Query.class), eq(Client.class))).thenReturn(Collections.emptyList());

        Page<Client> result = clientRepositoryCustom.searchClients("", pageable);

        assertNotNull(result);
        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(captor.capture(), eq(Client.class));
        assertFalse(captor.getValue().getQueryObject().containsKey("$text"));
    }

    @Test
    void searchLeads_WithNullKeyword_DoesNotUseTextQuery() {
        when(mongoTemplate.count(any(Query.class), eq(Lead.class))).thenReturn(0L);
        when(mongoTemplate.find(any(Query.class), eq(Lead.class))).thenReturn(Collections.emptyList());

        Page<Lead> result = leadRepositoryCustom.searchLeads(null, pageable);

        assertNotNull(result);
        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(captor.capture(), eq(Lead.class));
        assertFalse(captor.getValue().getQueryObject().containsKey("$text"));
    }

    @Test
    void searchProjects_WithBlankKeyword_DoesNotUseTextQuery() {
        when(mongoTemplate.count(any(Query.class), eq(Project.class))).thenReturn(0L);
        when(mongoTemplate.find(any(Query.class), eq(Project.class))).thenReturn(Collections.emptyList());

        Page<Project> result = projectRepositoryCustom.searchProjects(" \t ", pageable);

        assertNotNull(result);
        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(captor.capture(), eq(Project.class));
        assertFalse(captor.getValue().getQueryObject().containsKey("$text"));
    }

    @Test
    void searchUsers_ClampsExcessivePageSize() {
        when(mongoTemplate.count(any(Query.class), eq(User.class))).thenReturn(0L);
        when(mongoTemplate.find(any(Query.class), eq(User.class))).thenReturn(Collections.emptyList());

        Pageable excessive = PageRequest.of(1, 500);
        Page<User> result = userRepositoryCustom.searchUsers("test", excessive);

        assertNotNull(result);
        assertEquals(100, result.getPageable().getPageSize());
        assertEquals(1, result.getPageable().getPageNumber());
    }

    @Test
    void searchClients_ClampsExcessivePageSize() {
        when(mongoTemplate.count(any(Query.class), eq(Client.class))).thenReturn(0L);
        when(mongoTemplate.find(any(Query.class), eq(Client.class))).thenReturn(Collections.emptyList());

        Pageable excessive = PageRequest.of(0, 1000);
        Page<Client> result = clientRepositoryCustom.searchClients("acme", excessive);

        assertNotNull(result);
        assertEquals(100, result.getPageable().getPageSize());
    }

    @Test
    void searchLeads_ClampsExcessivePageSize() {
        when(mongoTemplate.count(any(Query.class), eq(Lead.class))).thenReturn(0L);
        when(mongoTemplate.find(any(Query.class), eq(Lead.class))).thenReturn(Collections.emptyList());

        Pageable excessive = PageRequest.of(2, 250);
        Page<Lead> result = leadRepositoryCustom.searchLeads("lead", excessive);

        assertNotNull(result);
        assertEquals(100, result.getPageable().getPageSize());
        assertEquals(2, result.getPageable().getPageNumber());
    }

    @Test
    void searchProjects_ClampsExcessivePageSize() {
        when(mongoTemplate.count(any(Query.class), eq(Project.class))).thenReturn(0L);
        when(mongoTemplate.find(any(Query.class), eq(Project.class))).thenReturn(Collections.emptyList());

        Pageable excessive = PageRequest.of(0, 9999);
        Page<Project> result = projectRepositoryCustom.searchProjects("crm", excessive);

        assertNotNull(result);
        assertEquals(100, result.getPageable().getPageSize());
    }
}
