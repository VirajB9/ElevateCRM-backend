package com.viraj.dmabackend.auth.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SecurityUtilsTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUserEmail_WhenNoAuthentication_ReturnsEmpty() {
        SecurityContextHolder.clearContext();
        assertEquals(Optional.empty(), SecurityUtils.getCurrentUserEmail());
    }

    @Test
    void getCurrentUserEmail_WhenPrincipalIsString_ReturnsTrimmedLowercaseEmail() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "  User@EXAMPLE.com  ",
                "credentials",
                AuthorityUtils.createAuthorityList("ROLE_USER")
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        Optional<String> email = SecurityUtils.getCurrentUserEmail();
        assertTrue(email.isPresent());
        assertEquals("user@example.com", email.get());
    }

    @Test
    void getCurrentUserEmail_WhenPrincipalIsUserDetails_ReturnsTrimmedLowercaseUsername() {
        User userDetails = new User(
                "  ADMIN@EXAMPLE.COM  ",
                "secret",
                AuthorityUtils.createAuthorityList("ROLE_ADMIN")
        );
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userDetails,
                "credentials",
                userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        Optional<String> email = SecurityUtils.getCurrentUserEmail();
        assertTrue(email.isPresent());
        assertEquals("admin@example.com", email.get());
    }

    @Test
    void getCurrentUserEmail_WhenPrincipalIsBlank_ReturnsEmpty() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "   ",
                "credentials",
                AuthorityUtils.NO_AUTHORITIES
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertEquals(Optional.empty(), SecurityUtils.getCurrentUserEmail());
    }
}
