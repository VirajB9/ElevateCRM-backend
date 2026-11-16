package com.viraj.dmabackend.config;

import com.mongodb.MongoClientSettings;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class MongoConfigTest {

    @Test
    void auditingDateTimeProvider_ReturnsUtcLocalDateTime() {
        MongoConfig mongoConfig = new MongoConfig();
        org.springframework.data.auditing.DateTimeProvider provider = mongoConfig.auditingDateTimeProvider();

        assertNotNull(provider);
        java.util.Optional<java.time.temporal.TemporalAccessor> result = provider.getNow();
        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof java.time.LocalDateTime);

        java.time.LocalDateTime nowUtc = java.time.LocalDateTime.now(java.time.ZoneOffset.UTC);
        java.time.LocalDateTime providerTime = (java.time.LocalDateTime) result.get();
        long diffSeconds = java.time.Duration.between(providerTime, nowUtc).abs().getSeconds();
        assertTrue(diffSeconds < 5, "Provider time should be close to current UTC time");
    }

    @Test
    void mongoConnectionCustomizer_AppliesPoolAndSocketSettings() {
        MongoConfig mongoConfig = new MongoConfig();
        MongoClientSettingsBuilderCustomizer customizer = mongoConfig.mongoConnectionCustomizer();

        assertNotNull(customizer);

        MongoClientSettings.Builder builder = MongoClientSettings.builder();
        customizer.customize(builder);
        MongoClientSettings settings = builder.build();

        assertEquals(50, settings.getConnectionPoolSettings().getMaxSize());
        assertEquals(5, settings.getConnectionPoolSettings().getMinSize());
        assertEquals(5000, settings.getConnectionPoolSettings().getMaxWaitTime(TimeUnit.MILLISECONDS));
        assertEquals(10000, settings.getSocketSettings().getConnectTimeout(TimeUnit.MILLISECONDS));
        assertEquals(10000, settings.getSocketSettings().getReadTimeout(TimeUnit.MILLISECONDS));
    }

    @Test
    void noOpTransactionManager_ExecutesTransactionsCleanlyWithoutExceptions() {
        MongoConfig mongoConfig = new MongoConfig();
        org.springframework.core.env.Environment env = mock(org.springframework.core.env.Environment.class);
        org.mockito.Mockito.when(env.matchesProfiles("prod")).thenReturn(false);

        PlatformTransactionManager txManager = mongoConfig.noOpTransactionManager(env);

        assertNotNull(txManager);
        assertTrue(txManager instanceof NoOpTransactionManager);

        TransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = txManager.getTransaction(def);
        assertNotNull(status);

        assertDoesNotThrow(() -> txManager.commit(status));

        TransactionStatus rollbackStatus = txManager.getTransaction(def);
        assertDoesNotThrow(() -> txManager.rollback(rollbackStatus));
    }

    @Test
    void noOpTransactionManager_ThrowsInProductionProfile() {
        MongoConfig mongoConfig = new MongoConfig();
        org.springframework.core.env.Environment env = mock(org.springframework.core.env.Environment.class);
        org.mockito.Mockito.when(env.matchesProfiles("prod")).thenReturn(true);

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalStateException.class,
                () -> mongoConfig.noOpTransactionManager(env)
        );
    }

    @Test
    void transactionManager_ReturnsMongoTransactionManager() {
        MongoConfig mongoConfig = new MongoConfig();
        MongoDatabaseFactory dbFactory = mock(MongoDatabaseFactory.class);

        MongoTransactionManager tm = mongoConfig.transactionManager(dbFactory);
        assertNotNull(tm);
        assertEquals(dbFactory, tm.getDatabaseFactory());
    }

    @Test
    void productionProfile_DisablesAutoIndexCreation() {
        org.yaml.snakeyaml.Yaml yaml = new org.yaml.snakeyaml.Yaml();
        java.io.InputStream inputStream = getClass().getClassLoader().getResourceAsStream("application-prod.yml");
        assertNotNull(inputStream, "application-prod.yml should exist");
        java.util.Map<String, Object> data = yaml.load(inputStream);

        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> spring = (java.util.Map<String, Object>) data.get("spring");
        assertNotNull(spring);
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> springData = (java.util.Map<String, Object>) spring.get("data");
        assertNotNull(springData);
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> mongodb = (java.util.Map<String, Object>) springData.get("mongodb");
        assertNotNull(mongodb);

        assertEquals(Boolean.FALSE, mongodb.get("auto-index-creation"));
    }
}
