package com.viraj.dmabackend.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class JwtFilterTest {

    private JwtUtil jwtUtil;
    private JwtFilter jwtFilter;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        jwtUtil = mock(JwtUtil.class);
        jwtFilter = new JwtFilter(jwtUtil);
        filterChain = mock(FilterChain.class);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_WithPasswordChangeRequired_AllowsChangePasswordOnContextPath() throws ServletException, IOException {
        String token = "valid-token";
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.extractEmail(token)).thenReturn("user@example.com");
        when(jwtUtil.extractRole(token)).thenReturn("EMPLOYEE");
        when(jwtUtil.extractPermissions(token)).thenReturn(List.of("client:read"));
        when(jwtUtil.extractPasswordChangeRequired(token)).thenReturn(true);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/dma/api/v1/auth/change-password");
        request.setContextPath("/dma");
        request.setServletPath("/api/v1/auth/change-password");
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtFilter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertEquals(200, response.getStatus());
    }

    @Test
    void doFilter_WithPasswordChangeRequired_AllowsChangePasswordWithTrailingSlash() throws ServletException, IOException {
        String token = "valid-token";
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.extractEmail(token)).thenReturn("user@example.com");
        when(jwtUtil.extractRole(token)).thenReturn("EMPLOYEE");
        when(jwtUtil.extractPermissions(token)).thenReturn(List.of("client:read"));
        when(jwtUtil.extractPasswordChangeRequired(token)).thenReturn(true);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/change-password/");
        request.setServletPath("/api/v1/auth/change-password/");
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtFilter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertEquals(200, response.getStatus());
    }

    @Test
    void doFilter_WithPasswordChangeRequired_BlocksOtherEndpointsWithStandardApiResponse() throws ServletException, IOException {
        String token = "valid-token";
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.extractEmail(token)).thenReturn("user@example.com");
        when(jwtUtil.extractRole(token)).thenReturn("EMPLOYEE");
        when(jwtUtil.extractPermissions(token)).thenReturn(List.of("client:read"));
        when(jwtUtil.extractPasswordChangeRequired(token)).thenReturn(true);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/clients");
        request.setServletPath("/api/v1/clients");
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtFilter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(any(), any());
        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString().contains("\"success\": false"));
        assertTrue(response.getContentAsString().contains("\"Password change required\""));
    }
}
