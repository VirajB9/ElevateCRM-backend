package com.viraj.dmabackend.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;

@SpringBootTest
@ActiveProfiles("test")
public abstract class AbstractMongoIntegrationTest {

    protected static final MongoDBContainer MONGO_CONTAINER;

    static {
        MONGO_CONTAINER = new MongoDBContainer("mongo:7.0.5");
        MONGO_CONTAINER.start();
    }

    @DynamicPropertySource
    static void setMongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO_CONTAINER::getReplicaSetUrl);
        registry.add("spring.data.mongodb.auto-index-creation", () -> "true");
        registry.add("spring.data.mongodb.transaction.enabled", () -> "true");
        registry.add("app.jwt.secret", () -> "testSecretKeyTestSecretKeyTestSecretKey123456");
        registry.add("app.jwt.expiration", () -> "900000");
        registry.add("app.jwt.refresh-expiration", () -> "604800000");
        registry.add("owner.initial-password", () -> "TestOwnerPass123!");
    }
}
