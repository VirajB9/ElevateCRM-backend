package com.viraj.dmabackend.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    void doFilter_WithValidRequestId_PreservesHeader() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Request-ID", "custom-trace-id-123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilter(request, response, filterChain);

        assertEquals("custom-trace-id-123", response.getHeader("X-Request-ID"));
    }

    @Test
    void doFilter_WithMaliciousCrlfHeader_ReplacesWithSafeUuid() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Request-ID", "injected-id\r\nInjected-Header: bad");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilter(request, response, filterChain);

        String resultHeader = response.getHeader("X-Request-ID");
        assertNotNull(resultHeader);
        assertNotEquals("injected-id\r\nInjected-Header: bad", resultHeader);
        assertTrue(resultHeader.matches("^[a-zA-Z0-9_-]{1,64}$"));
    }

    @Test
    void doFilter_WithTooLongHeader_ReplacesWithSafeUuid() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Request-ID", "a".repeat(100));
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilter(request, response, filterChain);

        String resultHeader = response.getHeader("X-Request-ID");
        assertNotNull(resultHeader);
        assertTrue(resultHeader.length() <= 64);
        assertTrue(resultHeader.matches("^[a-zA-Z0-9_-]{1,64}$"));
    }
}
