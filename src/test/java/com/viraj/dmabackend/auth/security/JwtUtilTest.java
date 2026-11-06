package com.viraj.dmabackend.auth.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 3600000L);
        jwtUtil.init();
    }

    @Test
    void generateToken_SetsUniqueJtiClaim() {
        String token1 = jwtUtil.generateToken("user1@example.com", "ADMIN", List.of("user:read"), false);
        String token2 = jwtUtil.generateToken("user1@example.com", "ADMIN", List.of("user:read"), false);

        String jti1 = jwtUtil.extractTokenId(token1);
        String jti2 = jwtUtil.extractTokenId(token2);

        assertNotNull(jti1);
        assertNotNull(jti2);
        assertNotEquals(jti1, jti2);

        // Verify it is a valid UUID
        assertDoesNotThrow(() -> UUID.fromString(jti1));
        assertDoesNotThrow(() -> UUID.fromString(jti2));
    }

    @Test
    void extractClaims_ExtractsAllFieldsCorrectly() {
        String token = jwtUtil.generateToken("user@example.com", "MANAGER", List.of("lead:read", "lead:write"), true);

        assertEquals("user@example.com", jwtUtil.extractEmail(token));
        assertEquals("MANAGER", jwtUtil.extractRole(token));
        assertEquals(List.of("lead:read", "lead:write"), jwtUtil.extractPermissions(token));
        assertTrue(jwtUtil.extractPasswordChangeRequired(token));
        assertTrue(jwtUtil.validateToken(token));
    }
}
