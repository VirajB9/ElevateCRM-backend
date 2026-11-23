package com.viraj.dmabackend.auth.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    public JwtFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this(jwtUtil, userDetailsService, new com.fasterxml.jackson.databind.ObjectMapper().findAndRegisterModules());
    }

    @org.springframework.beans.factory.annotation.Autowired
    public JwtFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService, com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.objectMapper = objectMapper != null ? objectMapper : new com.fasterxml.jackson.databind.ObjectMapper().findAndRegisterModules();
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        SecurityContextHolder.clearContext();

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || authHeader.length() < 7 || !authHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7).trim();
        if (token.isEmpty()) {
            SecurityContextHolder.clearContext();
            writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "INVALID_TOKEN", "Invalid JWT token");
            return;
        }

        try {
            if (!jwtUtil.validateToken(token)) {
                SecurityContextHolder.clearContext();
                writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "INVALID_TOKEN", "Invalid JWT token");
                return;
            }

            String email = jwtUtil.extractEmail(token);
            if (email == null) {
                SecurityContextHolder.clearContext();
                writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "INVALID_TOKEN", "Invalid JWT token");
                return;
            }

            CustomUserDetails userDetails;
            try {
                userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(email);
            } catch (Exception ex) {
                SecurityContextHolder.clearContext();
                writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "USER_NOT_FOUND", "User not found or inaccessible");
                return;
            }

            if (!userDetails.isEnabled() || !userDetails.isAccountNonLocked()) {
                SecurityContextHolder.clearContext();
                writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "USER_LOCKED", "User account is disabled or locked");
                return;
            }

            com.viraj.dmabackend.auth.entity.User user = userDetails.getUser();
            if (user != null && user.getTokensRevokedAt() != null) {
                java.util.Date issuedAt = jwtUtil.extractIssuedAt(token);
                if (issuedAt == null) {
                    SecurityContextHolder.clearContext();
                    writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "TOKEN_REVOKED", "Token has been revoked");
                    return;
                }
                long issuedAtMillis = issuedAt.getTime();
                long revokedAtMillis = user.getTokensRevokedAt().toInstant(java.time.ZoneOffset.UTC).toEpochMilli();
                if (issuedAtMillis <= revokedAtMillis) {
                    SecurityContextHolder.clearContext();
                    writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "TOKEN_REVOKED", "Token has been revoked");
                    return;
                }
            }

            java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> authorities = userDetails.getAuthorities();
            String tokenRole = jwtUtil.extractRole(token);
            List<String> tokenPermissions = jwtUtil.extractPermissions(token);
            if ((tokenRole != null && !tokenRole.isBlank()) || (tokenPermissions != null && !tokenPermissions.isEmpty())) {
                List<org.springframework.security.core.GrantedAuthority> tokenAuthorities = new java.util.ArrayList<>();
                if (tokenRole != null && !tokenRole.isBlank()) {
                    tokenAuthorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + tokenRole.trim()));
                }
                if (tokenPermissions != null) {
                    tokenPermissions.stream()
                            .filter(java.util.Objects::nonNull)
                            .filter(p -> !p.isBlank())
                            .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                            .forEach(tokenAuthorities::add);
                }
                authorities = tokenAuthorities;
            }

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                authorities
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            boolean passwordChangeRequired = (userDetails.getUser() != null && userDetails.getUser().isPasswordChangeRequired())
                    || jwtUtil.extractPasswordChangeRequired(token);
            String path = request.getServletPath();
            if (path != null) {
                path = path.replaceAll("/+", "/").replaceAll("/$", "");
            }

            if (passwordChangeRequired &&
                    !("/api/v1/auth/change-password".equals(path) || "/api/v1/auth/logout".equals(path) || "/api/v1/auth/refresh".equals(path))) {

                SecurityContextHolder.clearContext();
                writeErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "PASSWORD_CHANGE_REQUIRED", "Password change required");
                return;
            }

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException ex) {
            String path = request.getServletPath();
            if (path != null) {
                path = path.replaceAll("/+", "/").replaceAll("/$", "");
            }
            if ("/api/v1/auth/refresh".equals(path) || "/api/v1/auth/login".equals(path) || "/api/v1/auth/logout".equals(path)) {
                filterChain.doFilter(request, response);
                return;
            }
            log.debug("Expired JWT token received: {}", ex.getMessage());
            SecurityContextHolder.clearContext();
            writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "TOKEN_EXPIRED", "JWT token has expired");
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("Invalid JWT token received: {}", ex.getMessage());
            SecurityContextHolder.clearContext();
            writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "INVALID_TOKEN", "Invalid JWT token");
        }
    }

    private void writeErrorResponse(HttpServletResponse response, int status, String errorCode, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        String json = String.format(
                """
                {
                    "success": false,
                    "status": %d,
                    "errorCode": "%s",
                    "message": "%s",
                    "data": null,
                    "timestamp": "%s"
                }
                """, status, errorCode, message, java.time.Instant.now());
        response.getWriter().write(json);
    }
}