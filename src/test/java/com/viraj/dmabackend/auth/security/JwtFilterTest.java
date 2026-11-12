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
    private CustomUserDetailsService userDetailsService;
    private JwtFilter jwtFilter;
    private FilterChain filterChain;
    private com.viraj.dmabackend.auth.entity.User defaultUser;

    @BeforeEach
    void setUp() {
        jwtUtil = mock(JwtUtil.class);
        userDetailsService = mock(CustomUserDetailsService.class);
        jwtFilter = new JwtFilter(jwtUtil, userDetailsService);
        filterChain = mock(FilterChain.class);
        SecurityContextHolder.clearContext();

        defaultUser = com.viraj.dmabackend.auth.entity.User.builder()
                .id("u1")
                .email("user@example.com")
                .status(com.viraj.dmabackend.auth.enums.UserStatus.ACTIVE)
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(
                defaultUser,
                List.of(
                        new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_EMPLOYEE"),
                        new org.springframework.security.core.authority.SimpleGrantedAuthority("client:read")
                )
        );

        lenient().when(userDetailsService.loadUserByUsername("user@example.com")).thenReturn(userDetails);
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
    void doFilter_WithPasswordChangeRequired_AllowsRefreshEndpoint() throws ServletException, IOException {
        String token = "valid-token";
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.extractEmail(token)).thenReturn("user@example.com");
        when(jwtUtil.extractRole(token)).thenReturn("EMPLOYEE");
        when(jwtUtil.extractPermissions(token)).thenReturn(List.of("client:read"));
        when(jwtUtil.extractPasswordChangeRequired(token)).thenReturn(true);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/refresh");
        request.setServletPath("/api/v1/auth/refresh");
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

    @Test
    void doFilter_WithNullPermissions_SetsAuthenticationWithoutNPE() throws ServletException, IOException {
        String token = "valid-token";
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.extractEmail(token)).thenReturn("user@example.com");
        when(jwtUtil.extractPasswordChangeRequired(token)).thenReturn(false);

        CustomUserDetails userDetails = new CustomUserDetails(
                defaultUser,
                List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_EMPLOYEE"))
        );
        when(userDetailsService.loadUserByUsername("user@example.com")).thenReturn(userDetails);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/clients");
        request.setServletPath("/api/v1/clients");
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtFilter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        org.assertj.core.api.Assertions.assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_EMPLOYEE");
    }

    @Test
    void doFilter_PopulatesRoleAuthorityInSecurityContext() throws ServletException, IOException {
        String token = "valid-token";
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.extractEmail(token)).thenReturn("user@example.com");
        when(jwtUtil.extractPasswordChangeRequired(token)).thenReturn(false);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/clients");
        request.setServletPath("/api/v1/clients");
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtFilter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        org.assertj.core.api.Assertions.assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting("authority")
                .containsExactlyInAnyOrder("ROLE_EMPLOYEE", "client:read");
    }

    @Test
    void doFilter_WhenUserIsSuspendedOrInactive_DoesNotSetAuthentication() throws ServletException, IOException {
        String token = "valid-token";
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.extractEmail(token)).thenReturn("user@example.com");

        com.viraj.dmabackend.auth.entity.User suspendedUser = com.viraj.dmabackend.auth.entity.User.builder()
                .id("u1")
                .email("user@example.com")
                .status(com.viraj.dmabackend.auth.enums.UserStatus.SUSPENDED)
                .build();

        CustomUserDetails suspendedDetails = new CustomUserDetails(
                suspendedUser,
                List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_EMPLOYEE"))
        );
        when(userDetailsService.loadUserByUsername("user@example.com")).thenReturn(suspendedDetails);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/clients");
        request.setServletPath("/api/v1/clients");
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtFilter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        org.junit.jupiter.api.Assertions.assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilter_WhenTokenIssuedBeforeRevocation_DoesNotSetAuthentication() throws ServletException, IOException {
        String token = "valid-token";
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.extractEmail(token)).thenReturn("user@example.com");

        java.time.LocalDateTime revocationTime = java.time.LocalDateTime.now(java.time.ZoneOffset.UTC);
        defaultUser.setTokensRevokedAt(revocationTime);

        // Token issued 1 minute before revocation
        java.util.Date issuedAt = java.util.Date.from(revocationTime.minusMinutes(1).toInstant(java.time.ZoneOffset.UTC));
        when(jwtUtil.extractIssuedAt(token)).thenReturn(issuedAt);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/clients");
        request.setServletPath("/api/v1/clients");
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtFilter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        org.junit.jupiter.api.Assertions.assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilter_WhenTokenIssuedInSameSecondAsRevocation_AllowsAuthentication() throws ServletException, IOException {
        String token = "valid-token";
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.extractEmail(token)).thenReturn("user@example.com");

        java.time.LocalDateTime revocationTime = java.time.LocalDateTime.now(java.time.ZoneOffset.UTC)
                .truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
        defaultUser.setTokensRevokedAt(revocationTime);

        // Token issued in the same second as revocation (e.g. newly issued after password change)
        java.util.Date issuedAt = java.util.Date.from(revocationTime.toInstant(java.time.ZoneOffset.UTC));
        when(jwtUtil.extractIssuedAt(token)).thenReturn(issuedAt);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/clients");
        request.setServletPath("/api/v1/clients");
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtFilter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        org.junit.jupiter.api.Assertions.assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
