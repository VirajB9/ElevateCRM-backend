package com.viraj.dmabackend.auth.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<String> getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (principal == null || "anonymousUser".equals(principal)) {
            return Optional.empty();
        }

        if (principal instanceof UserDetails userDetails) {
            return Optional.ofNullable(userDetails.getUsername())
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .filter(s -> !s.isBlank());
        } else if (principal instanceof String email) {
            return Optional.of(email)
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .filter(s -> !s.isBlank());
        }

        return Optional.empty();
    }
}
