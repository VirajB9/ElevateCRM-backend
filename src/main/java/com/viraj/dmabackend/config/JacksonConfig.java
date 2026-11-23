package com.viraj.dmabackend.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

@Configuration
public class JacksonConfig {

    private static final DateTimeFormatter ISO_UTC_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer(Environment environment) {
        return builder -> {
            builder.serializationInclusion(JsonInclude.Include.NON_NULL);
            boolean isProd = false;
            String[] profiles = environment.getActiveProfiles();
            for (String p : profiles) {
                if (p != null) {
                    String lower = p.trim().toLowerCase();
                    if (lower.equals("prod") || lower.startsWith("prod-") || lower.startsWith("prod_") || lower.equals("production")) {
                        isProd = true;
                        break;
                    }
                }
            }

            if (isProd) {
                builder.featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            } else {
                builder.featuresToEnable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            }
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            builder.timeZone(TimeZone.getTimeZone(ZoneOffset.UTC));
            builder.serializers(new LocalDateTimeSerializer(ISO_UTC_FORMATTER));
        };
    }
}
