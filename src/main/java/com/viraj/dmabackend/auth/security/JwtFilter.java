package com.viraj.dmabackend.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

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

        String token = authHeader.substring(7);

        if (!jwtUtil.validateToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        String email = jwtUtil.extractEmail(token);
        if (email == null) {
            filterChain.doFilter(request, response);
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
        if (user.getTokensRevokedAt() != null) {
            java.util.Date issuedAt = jwtUtil.extractIssuedAt(token);
            if (issuedAt != null) {
                long issuedAtSeconds = issuedAt.getTime() / 1000;
                long revokedAtSeconds = user.getTokensRevokedAt().toEpochSecond(java.time.ZoneOffset.UTC);
                if (issuedAtSeconds < revokedAtSeconds) {
                    filterChain.doFilter(request, response);
                    return;
                }
            }
        }

        if (SecurityContextHolder
                .getContext()
                .getAuthentication() == null) {

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            userDetails.getAuthorities()
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request)
            );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);
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
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(
                    """
                    {
                        "success": false,
                        "message": "Password change required",
                        "data": null
                    }
                    """);
            return;
        }

        filterChain.doFilter(request, response);
    }
}