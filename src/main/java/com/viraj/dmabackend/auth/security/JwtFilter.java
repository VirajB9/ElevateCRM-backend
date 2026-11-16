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

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7).trim();
        if (token.isEmpty()) {
            SecurityContextHolder.clearContext();
            writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
            return;
        }

        try {
            if (!jwtUtil.validateToken(token)) {
                SecurityContextHolder.clearContext();
                writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
                return;
            }

            String email = jwtUtil.extractEmail(token);
            if (email == null) {
                SecurityContextHolder.clearContext();
                writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
                return;
            }

            CustomUserDetails userDetails;
            try {
                userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(email);
            } catch (Exception ex) {
                filterChain.doFilter(request, response);
                return;
            }

            if (!userDetails.isEnabled() || !userDetails.isAccountNonLocked()) {
                filterChain.doFilter(request, response);
                return;
            }

            com.viraj.dmabackend.auth.entity.User user = userDetails.getUser();
            if (user != null && user.getTokensRevokedAt() != null) {
                java.util.Date issuedAt = jwtUtil.extractIssuedAt(token);
                if (issuedAt != null) {
                    long issuedAtMillis = issuedAt.getTime();
                    long revokedAtMillis = user.getTokensRevokedAt().toInstant(java.time.ZoneOffset.UTC).toEpochMilli();
                    if (issuedAtMillis <= revokedAtMillis) {
                        filterChain.doFilter(request, response);
                        return;
                    }
                }
            }

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                userDetails.getAuthorities()
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
                writeErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Password change required");
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
            writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "JWT token has expired");
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("Invalid JWT token received: {}", ex.getMessage());
            SecurityContextHolder.clearContext();
            writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
        }
    }

    private void writeErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(String.format(
                """
                {
                    "success": false,
                    "message": "%s",
                    "data": null
                }
                """, message));
    }
}