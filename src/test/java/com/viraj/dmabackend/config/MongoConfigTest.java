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
    void mongoConnectionCustomizer_AppliesPoolAndSocketSettings() {
        MongoConfig mongoConfig = new MongoConfig();
        MongoClientSettingsBuilderCustomizer customizer = mongoConfig.mongoConnectionCustomizer();

        assertNotNull(customizer);

        MongoClientSettings.Builder builder = MongoClientSettings.builder();
        customizer.customize(builder);
        MongoClientSettings settings = builder.build();

        assertEquals(50, settings.getConnectionPoolSettings().getMaxSize());
        assertEquals(5, settings.getConnectionPoolSettings().getMinSize());
        assertEquals(10000, settings.getSocketSettings().getConnectTimeout(TimeUnit.MILLISECONDS));
        assertEquals(10000, settings.getSocketSettings().getReadTimeout(TimeUnit.MILLISECONDS));
    }

    @Test
    void noOpTransactionManager_ExecutesTransactionsCleanlyWithoutExceptions() {
        MongoConfig mongoConfig = new MongoConfig();
        PlatformTransactionManager txManager = mongoConfig.noOpTransactionManager();

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
    void transactionManager_ReturnsMongoTransactionManager() {
        MongoConfig mongoConfig = new MongoConfig();
        MongoDatabaseFactory dbFactory = mock(MongoDatabaseFactory.class);

        MongoTransactionManager tm = mongoConfig.transactionManager(dbFactory);
        assertNotNull(tm);
        assertEquals(dbFactory, tm.getDatabaseFactory());
    }
}
