package com.viraj.dmabackend.config;

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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int CAPACITY = 5;
    private static final int REFILL_TOKENS = 5;
    private static final Duration REFILL_DURATION = Duration.ofMinutes(1);
    private static final int MAX_CACHE_SIZE = 10000;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>(MAX_CACHE_SIZE);
    private final Object evictionLock = new Object();

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

        if (!("/api/v1/auth/login".equals(path)
                || "/api/v1/auth/refresh".equals(path))
                || !"POST".equalsIgnoreCase(request.getMethod())) {
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

        String errorMessage = "/api/v1/auth/refresh".equals(path)
                ? "Too many refresh attempts. Please try again later."
                : "Too many login attempts. Please try again later.";

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
        if (buckets.size() >= MAX_CACHE_SIZE && !buckets.containsKey(bucketKey)) {
            synchronized (evictionLock) {
                if (buckets.size() >= MAX_CACHE_SIZE && !buckets.containsKey(bucketKey)) {
                    var iterator = buckets.keySet().iterator();
                    int count = 0;
                    while (iterator.hasNext() && count < 500) {
                        iterator.next();
                        iterator.remove();
                        count++;
                    }
                }
            }
        }
        return buckets.computeIfAbsent(bucketKey, k -> createBucket());
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

    private String getClientIp(HttpServletRequest request) {
        // AUDIT-006 FIX: Trust Spring's ForwardedHeaderFilter rather than naively parsing raw headers to prevent trivial spoofing bypass.
        return request.getRemoteAddr();
    }
}