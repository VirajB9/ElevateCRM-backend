package com.viraj.dmabackend.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int CAPACITY = 5;
    private static final int REFILL_TOKENS = 5;
    private static final Duration REFILL_DURATION = Duration.ofMinutes(1);
    private static final int MAX_CACHE_SIZE = 10000;

    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
            .maximumSize(MAX_CACHE_SIZE)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();
        if (path != null) {
            path = path.replaceAll("/+", "/").replaceAll("/$", "");
        }

        boolean isChangePassword = "/api/v1/auth/change-password".equals(path);
        boolean isRateLimitedPath = "/api/v1/auth/login".equals(path)
                || "/api/v1/auth/refresh".equals(path)
                || isChangePassword;

        String method = request.getMethod();
        boolean isAllowedMethod = "POST".equalsIgnoreCase(method)
                || (isChangePassword && "PATCH".equalsIgnoreCase(method));

        if (!isRateLimitedPath || !isAllowedMethod) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);
        String bucketKey = path + ":" + clientIp;

        Bucket bucket = getBucket(bucketKey);

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            filterChain.doFilter(request, response);
            return;
        }

        long secondsToWait = Math.max(1, (probe.getNanosToWaitForRefill() + 999_999_999L) / 1_000_000_000L);
        response.setStatus(429); // HTTP 429 Too Many Requests
        response.setHeader("Retry-After", String.valueOf(secondsToWait));
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String errorMessage;
        if ("/api/v1/auth/refresh".equals(path)) {
            errorMessage = "Too many refresh attempts. Please try again later.";
        } else if ("/api/v1/auth/change-password".equals(path)) {
            errorMessage = "Too many password change attempts. Please try again later.";
        } else {
            errorMessage = "Too many login attempts. Please try again later.";
        }

        response.getWriter().write(
                """
                {
                    "success": false,
                    "message": "%s",
                    "data": null
                }
                """.formatted(errorMessage));
    }

    private Bucket getBucket(String bucketKey) {
        return buckets.get(bucketKey, k -> createBucket());
    }

    private Bucket createBucket() {
        Bandwidth limit = Bandwidth.classic(
                CAPACITY,
                Refill.greedy(
                        REFILL_TOKENS,
                        REFILL_DURATION));

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private static final java.util.Set<String> LOCAL_PROXIES = java.util.Set.of("127.0.0.1", "0:0:0:0:0:0:0:1", "::1", "localhost");

    private boolean isTrustedProxy(String ip) {
        if (ip == null || ip.isBlank()) {
            return false;
        }
        String clean = ip.trim();
        if (LOCAL_PROXIES.contains(clean)) {
            return true;
        }
        // IPv4 private address spaces (RFC 1918): 10.0.0.0/8, 192.168.0.0/16
        if (clean.startsWith("10.") || clean.startsWith("192.168.")) {
            return true;
        }
        // IPv4 private address space: 172.16.0.0/12
        if (clean.startsWith("172.")) {
            String[] parts = clean.split("\\.");
            if (parts.length >= 2) {
                try {
                    int secondOctet = Integer.parseInt(parts[1]);
                    if (secondOctet >= 16 && secondOctet <= 31) {
                        return true;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return false;
    }

    private String getClientIp(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        if (remoteAddr == null || remoteAddr.isBlank()) {
            remoteAddr = "unknown";
        } else {
            remoteAddr = remoteAddr.trim();
        }

        // Only trust proxy forwarding headers when request arrives from a trusted proxy or loopback
        if (isTrustedProxy(remoteAddr)) {
            String xRealIp = request.getHeader("X-Real-IP");
            if (xRealIp != null && !xRealIp.isBlank() && !"unknown".equalsIgnoreCase(xRealIp.trim())) {
                return xRealIp.trim();
            }

            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isBlank()) {
                String[] ips = xForwardedFor.split(",");
                for (String ip : ips) {
                    String trimmedIp = ip.trim();
                    if (!trimmedIp.isEmpty() && !"unknown".equalsIgnoreCase(trimmedIp)) {
                        return trimmedIp;
                    }
                }
            }
        }

        return remoteAddr;
    }
}
