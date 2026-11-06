package com.viraj.dmabackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
public class AuditorAwareConfig {

    @Bean
    public AuditorAware<String> auditorAware() {

        return () -> {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null
                    || !authentication.isAuthenticated()
                    || authentication instanceof AnonymousAuthenticationToken
                    || "anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.empty();
            }

            Object principal = authentication.getPrincipal();

            if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
                return Optional.ofNullable(userDetails.getUsername())
                        .map(String::trim)
                        .map(String::toLowerCase)
                        .filter(username -> !username.isBlank());
            } else if (principal instanceof String email) {
                return Optional.of(email)
                        .map(String::trim)
                        .map(String::toLowerCase)
                        .filter(value -> !value.isBlank());
            }

            return Optional.empty();
        };
    }
}
