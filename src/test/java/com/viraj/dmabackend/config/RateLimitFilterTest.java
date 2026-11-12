package com.viraj.dmabackend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class RateLimitFilterTest {

    private RateLimitFilter filter;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new RateLimitFilter();
        filterChain = mock(FilterChain.class);
    }

    @Test
    void doFilter_WithContextPath_StillAppliesRateLimit() throws ServletException, IOException {
        for (int i = 0; i < 5; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/context/api/v1/auth/login");
            request.setContextPath("/context");
            request.setServletPath("/api/v1/auth/login");
            request.setRemoteAddr("10.0.0.1");
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilter(request, response, filterChain);
            assertEquals(200, response.getStatus());
        }

        // 6th request from same IP should get 429
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/context/api/v1/auth/login");
        request.setContextPath("/context");
        request.setServletPath("/api/v1/auth/login");
        request.setRemoteAddr("10.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);
        assertEquals(429, response.getStatus());
        org.junit.jupiter.api.Assertions.assertNotNull(response.getHeader("Retry-After"));
        org.junit.jupiter.api.Assertions.assertTrue(response.getContentAsString().contains("Too many login attempts"));
        verify(filterChain, times(5)).doFilter(any(), any());

        // Refresh endpoint from same IP should have its own separate bucket
        MockHttpServletRequest refreshRequest = new MockHttpServletRequest("POST", "/api/v1/auth/refresh");
        refreshRequest.setServletPath("/api/v1/auth/refresh");
        refreshRequest.setRemoteAddr("10.0.0.1");
        MockHttpServletResponse refreshResponse = new MockHttpServletResponse();

        filter.doFilter(refreshRequest, refreshResponse, filterChain);
        assertEquals(200, refreshResponse.getStatus());
        verify(filterChain, times(6)).doFilter(any(), any());
    }

    @Test
    void doFilter_RefreshRateLimitExceeded_ReturnsRefreshSpecificMessage() throws ServletException, IOException {
        for (int i = 0; i < 5; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/refresh");
            request.setServletPath("/api/v1/auth/refresh");
            request.setRemoteAddr("10.0.0.5");
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilter(request, response, filterChain);
            assertEquals(200, response.getStatus());
        }

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/refresh");
        request.setServletPath("/api/v1/auth/refresh");
        request.setRemoteAddr("10.0.0.5");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);
        assertEquals(429, response.getStatus());
        org.junit.jupiter.api.Assertions.assertNotNull(response.getHeader("Retry-After"));
        org.junit.jupiter.api.Assertions.assertTrue(response.getContentAsString().contains("Too many refresh attempts"));
    }

    @Test
    void doFilter_WithTrailingSlash_StillAppliesRateLimit() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login/");
        request.setServletPath("/api/v1/auth/login/");
        request.setRemoteAddr("10.0.0.2");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(any(), any());
    }

    @Test
    void doFilter_NonRateLimitedPath_BypassesFilter() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/users");
        request.setServletPath("/api/v1/users");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
        assertEquals(200, response.getStatus());
    }

    @Test
    void doFilter_GetMethod_BypassesFilter() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/auth/login");
        request.setServletPath("/api/v1/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void cacheEviction_UnderConcurrentLoad_DoesNotCollapseCache() throws Exception {
        @SuppressWarnings("unchecked")
        com.github.benmanes.caffeine.cache.Cache<String, io.github.bucket4j.Bucket> bucketsCache =
                (com.github.benmanes.caffeine.cache.Cache<String, io.github.bucket4j.Bucket>)
                        org.springframework.test.util.ReflectionTestUtils.getField(filter, "buckets");
        java.util.concurrent.ConcurrentMap<String, io.github.bucket4j.Bucket> buckets = bucketsCache.asMap();

        io.github.bucket4j.Bucket dummyBucket = io.github.bucket4j.Bucket.builder()
                .addLimit(io.github.bucket4j.Bandwidth.classic(1, io.github.bucket4j.Refill.greedy(1, java.time.Duration.ofMinutes(1))))
                .build();

        for (int i = 0; i < 10000; i++) {
            buckets.put("/api/v1/auth/login:initial-" + i, dummyBucket);
        }

        int threadCount = 20;
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(threadCount);
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
                    request.setServletPath("/api/v1/auth/login");
                    request.setRemoteAddr("192.168.1." + index);
                    MockHttpServletResponse response = new MockHttpServletResponse();
                    filter.doFilter(request, response, filterChain);
                } catch (Exception ignored) {
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, java.util.concurrent.TimeUnit.SECONDS);
        executor.shutdown();

        org.junit.jupiter.api.Assertions.assertTrue(buckets.size() >= 9000,
                "Expected cache size to remain above 9000, but was: " + buckets.size());
    }

    @Test
    void rateLimit_CannotBeResetByFloodingOtherIps() throws ServletException, IOException {
        String attackerIp = "192.168.1.100";
        // Exhaust 5 tokens
        for (int i = 0; i < 5; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/v1/auth/login");
            req.setServletPath("/api/v1/auth/login");
            req.setRemoteAddr(attackerIp);
            MockHttpServletResponse res = new MockHttpServletResponse();
            filter.doFilter(req, res, filterChain);
            assertEquals(200, res.getStatus());
        }

        // 6th attempt is rate-limited (429)
        MockHttpServletRequest blockedReq = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        blockedReq.setServletPath("/api/v1/auth/login");
        blockedReq.setRemoteAddr(attackerIp);
        MockHttpServletResponse blockedRes = new MockHttpServletResponse();
        filter.doFilter(blockedReq, blockedRes, filterChain);
        assertEquals(429, blockedRes.getStatus());

        // Flood with other IPs to attempt cache eviction
        for (int i = 0; i < 100; i++) {
            MockHttpServletRequest floodReq = new MockHttpServletRequest("POST", "/api/v1/auth/login");
            floodReq.setServletPath("/api/v1/auth/login");
            floodReq.setRemoteAddr("10.10." + (i / 255) + "." + (i % 255));
            MockHttpServletResponse floodRes = new MockHttpServletResponse();
            filter.doFilter(floodReq, floodRes, filterChain);
        }

        // Attacker is STILL rate-limited! (No bucket reset)
        MockHttpServletRequest retryReq = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        retryReq.setServletPath("/api/v1/auth/login");
        retryReq.setRemoteAddr(attackerIp);
        MockHttpServletResponse retryRes = new MockHttpServletResponse();
        filter.doFilter(retryReq, retryRes, filterChain);
        assertEquals(429, retryRes.getStatus());
    }
}
