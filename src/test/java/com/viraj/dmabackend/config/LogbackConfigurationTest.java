package com.viraj.dmabackend.config;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LogbackConfigurationTest {

    @Test
    void logPattern_IncludesMdcRequestId() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("logback-spring.xml");
        assertNotNull(is, "logback-spring.xml should be present in classpath");
        String xml = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        assertTrue(xml.contains("%X{requestId}"), "LOG_PATTERN in logback-spring.xml must include %X{requestId} for correlation tracing");
    }
}
