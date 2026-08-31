package com.viraj.dmabackend.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneOffset;
import java.util.TimeZone;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // Ignore null fields from JSON responses
        objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);

        // Do not fail when incoming JSON contains unknown fields
        objectMapper.configure(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false
        );

        // Write dates using their configured format instead of timestamps
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Use UTC as the global timezone
        objectMapper.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC));

        // Register Java Time Module to support LocalDateTime/Instant
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

        return objectMapper;
    }
}
