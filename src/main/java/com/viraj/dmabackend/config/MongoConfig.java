package com.viraj.dmabackend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
@EnableMongoAuditing(auditorAwareRef = "auditorAware")
public class MongoConfig {

    @Bean
    @ConditionalOnProperty(name = "spring.data.mongodb.transaction.enabled", havingValue = "true")
    public MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
        log.info("MongoDB Multi-Document Transactions enabled via MongoTransactionManager");
        return new MongoTransactionManager(dbFactory);
    }

    @Bean
    @ConditionalOnMissingBean(PlatformTransactionManager.class)
    public PlatformTransactionManager noOpTransactionManager() {
        log.warn("MongoDB Multi-Document Transactions disabled. Using NoOpTransactionManager fallback for standalone MongoDB compatibility.");
        return new NoOpTransactionManager();
    }

    @Bean
    @Profile("prod")
    public MongoClientSettingsBuilderCustomizer mongoConnectionCustomizer() {
        return clientSettingsBuilder -> clientSettingsBuilder
                .applyToConnectionPoolSettings(pool -> pool.maxSize(50).minSize(5))
                .applyToSocketSettings(socket -> socket
                        .connectTimeout(10000, TimeUnit.MILLISECONDS)
                        .readTimeout(10000, TimeUnit.MILLISECONDS));
    }
}
