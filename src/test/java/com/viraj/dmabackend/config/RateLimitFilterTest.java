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
        verify(filterChain, times(5)).doFilter(any(), any());
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
}
