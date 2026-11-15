package com.viraj.dmabackend.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.viraj.dmabackend.auth.dto.LoginRequest;
import com.viraj.dmabackend.auth.entity.RefreshToken;
import com.viraj.dmabackend.auth.entity.Role;
import com.viraj.dmabackend.auth.entity.User;
import com.viraj.dmabackend.auth.enums.UserStatus;
import com.viraj.dmabackend.auth.repository.RefreshTokenRepository;
import com.viraj.dmabackend.auth.repository.RoleRepository;
import com.viraj.dmabackend.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private org.springframework.data.mongodb.core.MongoTemplate mongoTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private Role testRole;

    @BeforeEach
    void setUp() {
        mongoTemplate.dropCollection(RefreshToken.class);
        userRepository.deleteAll();
        roleRepository.deleteAll();

        testRole = new Role();
        testRole.setName("OWNER");
        testRole.setPermissionIds(List.of());
        testRole = roleRepository.save(testRole);

        testUser = new User();
        testUser.setEmail("admin@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRoleId(testRole.getId());
        testUser.setStatus(UserStatus.ACTIVE);
        testUser = userRepository.save(testUser);
    }

    @Test
    void login_WithValidCredentials_ReturnsTokens() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@example.com");
        request.setPassword("password123");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    void login_WithWrongPassword_ReturnsUnauthorized() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@example.com");
        request.setPassword("wrongpassword");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_WithValidToken_ReturnsNewToken() throws Exception {
        String tokenValue = UUID.randomUUID().toString();
        String tokenHash = RefreshToken.hashToken(tokenValue);
        
        RefreshToken refreshToken = RefreshToken.builder()
                .tokenHash(tokenHash)
                .userId(testUser.getId())
                .expiresAt(LocalDateTime.now().plusDays(1))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON).content("{\"refreshToken\": \"" + tokenValue + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists());

        RefreshToken oldToken = refreshTokenRepository.findByTokenHash(tokenHash).orElseThrow();
        org.junit.jupiter.api.Assertions.assertTrue(oldToken.isRevoked());
    }

    @Test
    void refresh_WithRevokedToken_ReturnsUnauthorized() throws Exception {
        String tokenValue = UUID.randomUUID().toString();
        String tokenHash = RefreshToken.hashToken(tokenValue);
        
        RefreshToken refreshToken = RefreshToken.builder()
                .tokenHash(tokenHash)
                .userId(testUser.getId())
                .expiresAt(LocalDateTime.now().plusDays(1))
                .revoked(true) // Revoked!
                .build();
        refreshTokenRepository.save(refreshToken);

        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON).content("{\"refreshToken\": \"" + tokenValue + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_PersistsHashedRefreshTokenAndNeverStoresPlaintext() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@example.com");
        loginRequest.setPassword("password123");

        String responseContent = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(responseContent);
        String rawRefreshToken = root.get("refreshToken").asText();
        org.junit.jupiter.api.Assertions.assertNotNull(rawRefreshToken);

        String expectedHash = RefreshToken.hashToken(rawRefreshToken);
        org.junit.jupiter.api.Assertions.assertEquals(64, expectedHash.length());

        // Token must be found by SHA-256 hash
        java.util.Optional<RefreshToken> stored = refreshTokenRepository.findByTokenHash(expectedHash);
        org.junit.jupiter.api.Assertions.assertTrue(stored.isPresent());
        org.junit.jupiter.api.Assertions.assertEquals(expectedHash, stored.get().getTokenHash());
    }
}
