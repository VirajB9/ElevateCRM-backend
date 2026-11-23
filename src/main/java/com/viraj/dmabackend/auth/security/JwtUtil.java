package com.viraj.dmabackend.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration}")
    private long expiration;

    private SecretKey key;
    private io.jsonwebtoken.JwtParser jwtParser;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8));
        this.jwtParser = Jwts.parser()
                .verifyWith(this.key)
                .clockSkewSeconds(60)
                .build();
    }

    public String generateToken(
            String email,
            String role,
            List<String> permissions,
            boolean passwordChangeRequired) {
        return generateToken(email, role, permissions, passwordChangeRequired, new Date());
    }

    public String generateToken(
            String email,
            String role,
            List<String> permissions,
            boolean passwordChangeRequired,
            Date issuedAt) {

        return Jwts.builder()
                .id(java.util.UUID.randomUUID().toString())
                .subject(email)
                .claim("role", role)
                .claim("permissions", permissions)
                .claim("passwordChangeRequired", passwordChangeRequired)
                .claim("iat_ms", issuedAt.getTime())
                .issuedAt(issuedAt)
                .expiration(
                        new Date(issuedAt.getTime() + expiration)
                )
                .signWith(key)
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    public String extractTokenId(String token) {
        return extractClaims(token).getId();
    }

    public Date extractIssuedAt(String token) {
        Claims claims = extractClaims(token);
        Object iatMs = claims.get("iat_ms");
        if (iatMs instanceof Number number) {
            return new Date(number.longValue());
        }
        return claims.getIssuedAt();
    }

    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    @SuppressWarnings("unchecked")
    public List<String> extractPermissions(String token) {
        List<String> permissions = extractClaims(token).get("permissions", List.class);
        return permissions != null ? permissions : java.util.Collections.emptyList();
    }

    public boolean extractPasswordChangeRequired(String token) {
        Boolean value = extractClaims(token).get("passwordChangeRequired", Boolean.class);
        return value != null && value;
    }

    public boolean validateToken(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw e;
        } catch (Exception e) {
            return false;
        }
    }

    public Claims extractAllClaims(String token) {
        return extractClaims(token);
    }

    private Claims extractClaims(String token) {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            Object cached = request.getAttribute("CACHED_JWT_CLAIMS");
            if (cached instanceof Claims claims && token.equals(request.getAttribute("CACHED_JWT_TOKEN"))) {
                return claims;
            }
        }

        io.jsonwebtoken.JwtParser parser = this.jwtParser;
        if (parser == null) {
            parser = Jwts.parser()
                    .verifyWith(this.key != null ? this.key : Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                    .clockSkewSeconds(60)
                    .build();
        }

        Claims claims = parser
                .parseSignedClaims(token)
                .getPayload();

        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            request.setAttribute("CACHED_JWT_CLAIMS", claims);
            request.setAttribute("CACHED_JWT_TOKEN", token);
        }

        return claims;
    }

    private final SecureRandom secureRandom = new SecureRandom();

    public String generateRefreshToken() {

        byte[] randomBytes = new byte[64];
        secureRandom.nextBytes(randomBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }
}
