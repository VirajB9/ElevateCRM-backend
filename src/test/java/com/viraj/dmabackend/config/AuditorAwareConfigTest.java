package com.viraj.dmabackend.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuditorAwareConfigTest {

    private final AuditorAware<String> auditorAware = new AuditorAwareConfig().auditorAware();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentAuditor_WhenNoAuthentication_ReturnsEmpty() {
        SecurityContextHolder.clearContext();
        assertEquals(Optional.empty(), auditorAware.getCurrentAuditor());
    }

    @Test
    void getCurrentAuditor_WhenAnonymousAuthenticationToken_ReturnsEmpty() {
        AnonymousAuthenticationToken auth = new AnonymousAuthenticationToken(
                "key",
                "anonymousUser",
                AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertEquals(Optional.empty(), auditorAware.getCurrentAuditor());
    }

    @Test
    void getCurrentAuditor_WhenPrincipalIsAnonymousUserString_ReturnsEmpty() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "anonymousUser",
                "credentials",
                AuthorityUtils.NO_AUTHORITIES);
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertEquals(Optional.empty(), auditorAware.getCurrentAuditor());
    }

    @Test
    void getCurrentAuditor_WhenAuthenticatedUser_ReturnsEmail() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "john.doe@example.com",
                "credentials",
                AuthorityUtils.createAuthorityList("ROLE_USER"));
        SecurityContextHolder.getContext().setAuthentication(auth);

        Optional<String> auditor = auditorAware.getCurrentAuditor();
        assertTrue(auditor.isPresent());
        assertEquals("john.doe@example.com", auditor.get());
    }
}
