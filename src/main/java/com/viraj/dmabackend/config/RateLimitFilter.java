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
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Rate limit filter using Bucket4j and Caffeine.
 *
 * NOTE FOR HORIZONTALLY SCALED / MULTI-INSTANCE DEPLOYMENTS:
 * This rate limiter utilizes an in-memory Caffeine cache scoped to the local JVM process.
 * In clustered environments running multiple application replicas behind a load balancer,
 * token consumption is per-node. For strict cluster-wide rate limiting, enforce rate limits
 * at the reverse proxy / API gateway layer (e.g., Nginx limit_req_zone, AWS WAF, or Cloudflare),
 * or configure a distributed Bucket4j proxy backend (e.g., Redis).
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int CAPACITY = 5;
    private static final int REFILL_TOKENS = 5;
    private static final Duration REFILL_DURATION = Duration.ofMinutes(1);
    private static final int MAX_CACHE_SIZE = 10000;
    private static final int MAX_BLOCKED_CACHE_SIZE = 100000;

    @org.springframework.beans.factory.annotation.Value("${app.rate-limit.trusted-proxies:198.51.100.1}")
    private List<String> trustedProxies = List.of("198.51.100.1");

    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper =
            new com.fasterxml.jackson.databind.ObjectMapper().findAndRegisterModules();

    public void setTrustedProxies(List<String> trustedProxies) {
        this.trustedProxies = trustedProxies;
    }

    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
            .maximumSize(MAX_CACHE_SIZE)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build();

    private final Cache<String, Long> blockedUntil = Caffeine.newBuilder()
            .maximumSize(MAX_BLOCKED_CACHE_SIZE)
            .expireAfterWrite(1, TimeUnit.MINUTES)
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
                || "/api/v1/auth/logout".equals(path)
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

        Long blockedUntilEpoch = blockedUntil.getIfPresent(bucketKey);
        if (blockedUntilEpoch != null && System.currentTimeMillis() < blockedUntilEpoch) {
            long remainingMillis = blockedUntilEpoch - System.currentTimeMillis();
            long secondsToWait = Math.max(1, (remainingMillis + 999) / 1000);
            sendRateLimitResponse(response, path, secondsToWait);
            return;
        }

        Bucket bucket = getBucket(bucketKey);

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            filterChain.doFilter(request, response);
            return;
        }

        long secondsToWait = Math.max(1, (probe.getNanosToWaitForRefill() + 999_999_999L) / 1_000_000_000L);
        blockedUntil.put(bucketKey, System.currentTimeMillis() + (secondsToWait * 1000L));
        sendRateLimitResponse(response, path, secondsToWait);
    }

    private void sendRateLimitResponse(HttpServletResponse response, String path, long secondsToWait) throws IOException {
        response.setStatus(429); // HTTP 429 Too Many Requests
        response.setHeader("Retry-After", String.valueOf(secondsToWait));
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String errorMessage;
        if ("/api/v1/auth/refresh".equals(path)) {
            errorMessage = "Too many refresh attempts. Please try again later.";
        } else if ("/api/v1/auth/logout".equals(path)) {
            errorMessage = "Too many logout attempts. Please try again later.";
        } else if ("/api/v1/auth/change-password".equals(path)) {
            errorMessage = "Too many password change attempts. Please try again later.";
        } else {
            errorMessage = "Too many login attempts. Please try again later.";
        }

        response.setContentType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        com.viraj.dmabackend.common.response.ApiResponse<Void> apiResponse = com.viraj.dmabackend.common.response.ApiResponse.<Void>builder()
                .success(false)
                .status(429)
                .errorCode("RATE_LIMIT_EXCEEDED")
                .message(errorMessage)
                .data(null)
                .timestamp(java.time.Instant.now())
                .build();
        objectMapper.writeValue(response.getWriter(), apiResponse);
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

    private static final java.util.regex.Pattern IP_PATTERN = java.util.regex.Pattern.compile(
            "^([0-9]{1,3}\\.){3}[0-9]{1,3}$|^([0-9a-fA-F]{0,4}:){1,7}[0-9a-fA-F]{0,4}$"
    );

    private boolean isTrustedProxy(String ip) {
        if (ip == null || ip.isBlank()) {
            return false;
        }
        String clean = ip.trim();
        if (!IP_PATTERN.matcher(clean).matches()) {
            return false;
        }
        if (LOCAL_PROXIES.contains(clean)) {
            return true;
        }
        if (trustedProxies != null && trustedProxies.contains(clean)) {
            return true;
        }
        if (clean.startsWith("10.") || clean.startsWith("192.168.") || clean.startsWith("127.") || clean.startsWith("169.254.")) {
            return true;
        }
        if (clean.startsWith("172.")) {
            String[] parts = clean.split("\\.");
            if (parts.length >= 2) {
                try {
                    int second = Integer.parseInt(parts[1]);
                    if (second >= 16 && second <= 31) {
                        return true;
                    }
                } catch (NumberFormatException ignored) {}
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

        // Only trust proxy forwarding headers when request arrives from an explicitly trusted proxy
        if (isTrustedProxy(remoteAddr)) {
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isBlank()) {
                String[] ips = xForwardedFor.split(",");
                // Walk right-to-left to find the first untrusted client IP in the proxy chain,
                // preventing spoofing via client-prepended leftmost IPs.
                for (int i = ips.length - 1; i >= 0; i--) {
                    String trimmedIp = ips[i].trim();
                    if (!trimmedIp.isEmpty() && !"unknown".equalsIgnoreCase(trimmedIp) && !isTrustedProxy(trimmedIp)) {
                        return trimmedIp;
                    }
                }
                for (String ip : ips) {
                    String trimmedIp = ip.trim();
                    if (!trimmedIp.isEmpty() && !"unknown".equalsIgnoreCase(trimmedIp)) {
                        return trimmedIp;
                    }
                }
            }

            String xRealIp = request.getHeader("X-Real-IP");
            if (xRealIp != null && !xRealIp.isBlank() && !"unknown".equalsIgnoreCase(xRealIp.trim()) && !isTrustedProxy(xRealIp.trim())) {
                return xRealIp.trim();
            }
        }

        return remoteAddr;
    }
}
