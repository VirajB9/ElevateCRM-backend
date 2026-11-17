package com.viraj.dmabackend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.transaction.PlatformTransactionManager;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
@EnableMongoAuditing(auditorAwareRef = "auditorAware", dateTimeProviderRef = "auditingDateTimeProvider")
public class MongoConfig {

    @Bean
    public DateTimeProvider auditingDateTimeProvider() {
        return () -> Optional.of(LocalDateTime.now(ZoneOffset.UTC));
    }

    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(new Converter<LocalDateTime, Date>() {
            @Override
            public Date convert(LocalDateTime source) {
                return Date.from(source.toInstant(ZoneOffset.UTC));
            }
        });
        converters.add(new Converter<Date, LocalDateTime>() {
            @Override
            public LocalDateTime convert(Date source) {
                return LocalDateTime.ofInstant(source.toInstant(), ZoneOffset.UTC);
            }
        });
        return new MongoCustomConversions(converters);
    }

    @Bean
    @ConditionalOnProperty(name = "spring.data.mongodb.transaction.enabled", havingValue = "true")
    public MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
        log.info("MongoDB Multi-Document Transactions enabled via MongoTransactionManager");
        return new MongoTransactionManager(dbFactory);
    }

    @Bean
    @ConditionalOnMissingBean(PlatformTransactionManager.class)
    public PlatformTransactionManager noOpTransactionManager(org.springframework.core.env.Environment environment) {
        if (environment.matchesProfiles("prod")) {
            throw new IllegalStateException("MongoDB Multi-Document Transactions must be enabled in production profile ('prod'). Set spring.data.mongodb.transaction.enabled=true.");
        }
        log.warn("MongoDB Multi-Document Transactions disabled. Using NoOpTransactionManager fallback for standalone MongoDB compatibility.");
        return new NoOpTransactionManager();
    }

    @Bean
    @Profile("prod")
    public MongoClientSettingsBuilderCustomizer mongoConnectionCustomizer() {
        return clientSettingsBuilder -> clientSettingsBuilder
                .applyToConnectionPoolSettings(pool -> pool
                        .maxSize(100)
                        .minSize(10)
                        .maxWaitTime(10000, TimeUnit.MILLISECONDS))
                .applyToSocketSettings(socket -> socket
                        .connectTimeout(10000, TimeUnit.MILLISECONDS)
                        .readTimeout(10000, TimeUnit.MILLISECONDS));
    }
}
