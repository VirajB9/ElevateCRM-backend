package com.viraj.dmabackend.config;

import com.viraj.dmabackend.auth.security.JwtFilter;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SecurityActuatorTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Test
    void actuatorHealth_isPermittedWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void securityFilterChain_hasRateLimitFilterBeforeJwtFilter() {
        List<Filter> filters = securityFilterChain.getFilters();
        int rateLimitIndex = -1;
        int jwtIndex = -1;

        for (int i = 0; i < filters.size(); i++) {
            Filter filter = filters.get(i);
            if (filter instanceof RateLimitFilter) {
                rateLimitIndex = i;
            } else if (filter instanceof JwtFilter) {
                jwtIndex = i;
            }
        }

        assertThat(rateLimitIndex)
                .as("RateLimitFilter must be present in SecurityFilterChain")
                .isGreaterThanOrEqualTo(0);
        assertThat(jwtIndex)
                .as("JwtFilter must be present in SecurityFilterChain")
                .isGreaterThanOrEqualTo(0);
        assertThat(rateLimitIndex)
                .as("RateLimitFilter must execute before JwtFilter")
                .isLessThan(jwtIndex);
    }
}
