package com.viraj.dmabackend.client.controller;

import com.viraj.dmabackend.client.dto.ClientResponse;
import com.viraj.dmabackend.client.dto.CreateClientRequest;
import com.viraj.dmabackend.client.dto.UpdateClientRequest;
import com.viraj.dmabackend.client.enums.ClientStatus;
import com.viraj.dmabackend.client.service.ClientService;
import com.viraj.dmabackend.controller.BaseControllerWebMvcTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClientController.class)
class ClientControllerTest extends BaseControllerWebMvcTest {

    @MockBean
    private ClientService clientService;

    @Test
    @WithMockUser(authorities = "client:create")
    void createClient_whenAuthorized_returnsCreated() throws Exception {
        CreateClientRequest request = new CreateClientRequest();
        request.setCompanyName("Alpha Corp");
        request.setContactPerson("Alice");
        request.setEmail("alice@alpha.com");
        request.setPhoneNumber("9876543210");

        when(clientService.createClient(any())).thenReturn(ClientResponse.builder().id("c-1").build());

        mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(authorities = "client:read")
    void createClient_whenInsufficientAuthority_returnsForbidden() throws Exception {
        CreateClientRequest request = new CreateClientRequest();
        request.setCompanyName("Alpha Corp");
        request.setContactPerson("Alice");
        request.setEmail("alice@alpha.com");
        request.setPhoneNumber("9876543210");

        mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createClient_whenUnauthenticated_returnsUnauthorized() throws Exception {
        CreateClientRequest request = new CreateClientRequest();
        request.setCompanyName("Alpha Corp");
        request.setContactPerson("Alice");
        request.setEmail("alice@alpha.com");
        request.setPhoneNumber("9876543210");

        mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "client:read")
    void getAllClients_whenAuthorized_returnsOk() throws Exception {
        when(clientService.getAllClients(any())).thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/v1/clients"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "client:read")
    void getClientById_whenAuthorized_returnsOk() throws Exception {
        when(clientService.getClientById("c-1")).thenReturn(ClientResponse.builder().id("c-1").build());

        mockMvc.perform(get("/api/v1/clients/c-1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "client:read")
    void searchClients_whenAuthorized_returnsOk() throws Exception {
        when(clientService.searchClients(eq("alpha"), any())).thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/v1/clients/search")
                        .param("keyword", "alpha"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "client:read")
    void filterClientsByStatus_whenAuthorized_returnsOk() throws Exception {
        when(clientService.filterClientsByStatus(eq(ClientStatus.ACTIVE), any())).thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/v1/clients/status")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "client:update")
    void updateClient_whenAuthorized_returnsOk() throws Exception {
        UpdateClientRequest request = new UpdateClientRequest();
        request.setCompanyName("Updated Corp");
        request.setContactPerson("Bob");
        request.setEmail("bob@example.com");
        request.setPhoneNumber("+1234567890");
        request.setStatus(com.viraj.dmabackend.client.enums.ClientStatus.ACTIVE);

        when(clientService.updateClient(eq("c-1"), any())).thenReturn(ClientResponse.builder().id("c-1").build());

        mockMvc.perform(put("/api/v1/clients/c-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "client:delete")
    void deleteClient_whenAuthorized_returnsNoContent() throws Exception {
        doNothing().when(clientService).deleteClient("c-1");

        mockMvc.perform(delete("/api/v1/clients/c-1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = "client:read")
    void deleteClient_whenInsufficientAuthority_returnsForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/clients/c-1"))
                .andExpect(status().isForbidden());
    }
}
