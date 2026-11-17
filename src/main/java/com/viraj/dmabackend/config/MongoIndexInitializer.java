package com.viraj.dmabackend.config;

import com.viraj.dmabackend.auth.entity.Permission;
import com.viraj.dmabackend.auth.entity.RefreshToken;
import com.viraj.dmabackend.auth.entity.Role;
import com.viraj.dmabackend.auth.entity.User;
import com.viraj.dmabackend.client.entity.Client;
import com.viraj.dmabackend.common.counter.Counter;
import com.viraj.dmabackend.invoice.entity.Invoice;
import com.viraj.dmabackend.invoice.entity.InvoiceHistory;
import com.viraj.dmabackend.lead.entity.Lead;
import com.viraj.dmabackend.menu.entity.Menu;
import com.viraj.dmabackend.project.entity.Project;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MongoIndexInitializer implements ApplicationRunner {

    private final MongoTemplate mongoTemplate;
    private final Environment environment;

    private static final List<Class<?>> DOMAIN_ENTITIES = List.of(
            User.class,
            Role.class,
            Permission.class,
            RefreshToken.class,
            Client.class,
            Lead.class,
            Project.class,
            Invoice.class,
            InvoiceHistory.class,
            Menu.class,
            Counter.class
    );

    @Override
    public void run(ApplicationArguments args) {
        boolean autoIndexCreation = Boolean.parseBoolean(
                environment.getProperty("spring.data.mongodb.auto-index-creation", "true")
        );

        if (!autoIndexCreation || environment.matchesProfiles("prod")) {
            log.info("Ensuring MongoDB indexes across domain collections (auto-index-creation disabled or prod profile active)...");
            ensureIndexes();
        }
    }

    public void ensureIndexes() {
        IndexResolver resolver = new MongoPersistentEntityIndexResolver(mongoTemplate.getConverter().getMappingContext());

        for (Class<?> entityClass : DOMAIN_ENTITIES) {
            try {
                IndexOperations indexOps = mongoTemplate.indexOps(entityClass);
                resolver.resolveIndexFor(entityClass).forEach(indexDefinition -> {
                    try {
                        indexOps.ensureIndex(indexDefinition);
                    } catch (Exception e) {
                        if (environment.matchesProfiles("prod")) {
                            log.error("CRITICAL: Failed to ensure index on {}: {}", entityClass.getSimpleName(), e.getMessage(), e);
                            throw new IllegalStateException("Failed to initialize required MongoDB index for " + entityClass.getSimpleName() + ": " + e.getMessage(), e);
                        }
                        log.warn("Failed to ensure index on {}: {}", entityClass.getSimpleName(), e.getMessage());
                    }
                });
                log.debug("Indexes verified for entity: {}", entityClass.getSimpleName());
            } catch (Exception e) {
                if (environment.matchesProfiles("prod") && !isReflectionAccessError(e)) {
                    log.error("CRITICAL: Failed to process indexes for entity {}: {}", entityClass.getSimpleName(), e.getMessage(), e);
                    throw new IllegalStateException("Failed to initialize required MongoDB index for " + entityClass.getSimpleName() + ": " + e.getMessage(), e);
                }
                log.error("Failed to process indexes for entity {}: {}", entityClass.getSimpleName(), e.getMessage());
            }
        }
        log.info("MongoDB domain indexes successfully verified and initialized.");
    }

    private boolean isReflectionAccessError(Throwable t) {
        Throwable current = t;
        while (current != null) {
            if (current instanceof java.lang.reflect.InaccessibleObjectException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
