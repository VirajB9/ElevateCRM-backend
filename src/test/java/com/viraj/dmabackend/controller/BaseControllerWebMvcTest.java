package com.viraj.dmabackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.viraj.dmabackend.auth.security.JwtFilter;
import com.viraj.dmabackend.config.CorsProperties;
import com.viraj.dmabackend.config.RateLimitFilter;
import com.viraj.dmabackend.config.SecurityConfig;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@Import(SecurityConfig.class)
public abstract class BaseControllerWebMvcTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    protected JwtFilter jwtFilter;

    @MockBean
    protected RateLimitFilter rateLimitFilter;

    @MockBean
    protected CorsProperties corsProperties;

    @BeforeEach
    void setUpFilters() throws Exception {
        SecurityContextHolder.clearContext();
        doAnswer(invocation -> {
            ((FilterChain) invocation.getArgument(2)).doFilter(
                    invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtFilter).doFilter(any(), any(), any());

        doAnswer(invocation -> {
            ((FilterChain) invocation.getArgument(2)).doFilter(
                    invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(rateLimitFilter).doFilter(any(), any(), any());
    }

    @AfterEach
    void tearDownSecurity() {
        SecurityContextHolder.clearContext();
    }
}
