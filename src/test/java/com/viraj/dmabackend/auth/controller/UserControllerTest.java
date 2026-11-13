package com.viraj.dmabackend.auth.controller;

import com.viraj.dmabackend.auth.dto.*;
import com.viraj.dmabackend.auth.enums.UserStatus;
import com.viraj.dmabackend.auth.service.UserService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest extends BaseControllerWebMvcTest {

    @MockBean
    private UserService userService;

    @Test
    @WithMockUser(authorities = "user:create")
    void createUser_whenAuthorized_returnsCreated() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setPhoneNumber("9876543210");
        request.setRoleId("role-1");

        CreateUserResponse response = CreateUserResponse.builder()
                .user(UserResponse.builder().id("u-1").email("john.doe@example.com").build())
                .temporaryPassword("TempPass123!")
                .build();

        when(userService.createUser(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(authorities = "user:read")
    void createUser_whenInsufficientAuthority_returnsForbidden() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setPhoneNumber("9876543210");
        request.setRoleId("role-1");

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createUser_whenUnauthenticated_returnsUnauthorized() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setPhoneNumber("9876543210");
        request.setRoleId("role-1");

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "user:create")
    void createUser_whenInvalidPayload_returnsBadRequest() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("invalid-email");

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "user:read")
    void getAllUsers_whenAuthorized_returnsOk() throws Exception {
        when(userService.getAllUsers(any())).thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "lead:read")
    void getAllUsers_whenInsufficientAuthority_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "user:read")
    void getUserById_whenAuthorized_returnsOk() throws Exception {
        when(userService.getUserById("u-1")).thenReturn(UserResponse.builder().id("u-1").build());

        mockMvc.perform(get("/api/v1/users/u-1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "user:update")
    void updateUser_whenAuthorized_returnsOk() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("John");
        request.setLastName("Smith");
        request.setPhoneNumber("9876543210");
        request.setRoleId("role-2");
        request.setVersion(1L);

        when(userService.updateUser(eq("u-1"), any())).thenReturn(UserResponse.builder().id("u-1").build());

        mockMvc.perform(put("/api/v1/users/u-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "user:read")
    void updateUser_whenInsufficientAuthority_returnsForbidden() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("John");
        request.setLastName("Smith");
        request.setPhoneNumber("9876543210");
        request.setRoleId("role-2");
        request.setVersion(1L);

        mockMvc.perform(put("/api/v1/users/u-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "user:update")
    void updateUserStatus_whenAuthorized_returnsOk() throws Exception {
        UpdateUserStatusRequest request = new UpdateUserStatusRequest();
        request.setStatus(UserStatus.SUSPENDED);
        request.setVersion(1L);

        when(userService.updateUserStatus(eq("u-1"), any())).thenReturn(UserResponse.builder().id("u-1").build());

        mockMvc.perform(patch("/api/v1/users/u-1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "user:read")
    void updateUserStatus_whenInsufficientAuthority_returnsForbidden() throws Exception {
        UpdateUserStatusRequest request = new UpdateUserStatusRequest();
        request.setStatus(UserStatus.SUSPENDED);
        request.setVersion(1L);

        mockMvc.perform(patch("/api/v1/users/u-1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "user:delete")
    void softDeleteUser_whenAuthorized_returnsOk() throws Exception {
        when(userService.softDeleteUser("u-1")).thenReturn(UserResponse.builder().id("u-1").build());

        mockMvc.perform(delete("/api/v1/users/u-1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "user:read")
    void softDeleteUser_whenInsufficientAuthority_returnsForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/users/u-1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "user:read")
    void searchUsers_whenAuthorized_returnsOk() throws Exception {
        when(userService.searchUsers(eq("john"), any())).thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/v1/users/search")
                        .param("keyword", "john"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "user:read")
    void filterUsersByStatus_whenAuthorized_returnsOk() throws Exception {
        when(userService.filterUsersByStatus(eq(UserStatus.ACTIVE), any())).thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/v1/users/filter")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "user:read")
    void getUserById_withInvalidIdPattern_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/users/invalid!user@id"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "user:read")
    void searchUsers_withOversizedKeyword_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/users/search")
                        .param("keyword", "a".repeat(101)))
                .andExpect(status().isBadRequest());
    }
}
