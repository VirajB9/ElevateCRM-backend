package com.viraj.dmabackend.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MongoIndexInitializerTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private Environment environment;

    @Mock
    private ApplicationArguments args;

    @Mock
    private MongoConverter mongoConverter;

    @Mock
    private IndexOperations indexOperations;

    @Test
    void run_whenAutoIndexCreationIsFalse_ensuresIndexes() {
        when(environment.getProperty("spring.data.mongodb.auto-index-creation", "true")).thenReturn("false");

        MongoMappingContext mappingContext = new MongoMappingContext();
        when(mongoTemplate.getConverter()).thenReturn(mongoConverter);
        doReturn(mappingContext).when(mongoConverter).getMappingContext();
        when(mongoTemplate.indexOps(any(Class.class))).thenReturn(indexOperations);

        MongoIndexInitializer initializer = new MongoIndexInitializer(mongoTemplate, environment);
        initializer.run(args);

        verify(mongoTemplate, atLeastOnce()).indexOps(any(Class.class));
    }

    @Test
    void run_whenProdProfileActive_ensuresIndexes() {
        when(environment.getProperty("spring.data.mongodb.auto-index-creation", "true")).thenReturn("true");
        when(environment.matchesProfiles("prod")).thenReturn(true);

        MongoMappingContext mappingContext = new MongoMappingContext();
        when(mongoTemplate.getConverter()).thenReturn(mongoConverter);
        doReturn(mappingContext).when(mongoConverter).getMappingContext();
        when(mongoTemplate.indexOps(any(Class.class))).thenReturn(indexOperations);

        MongoIndexInitializer initializer = new MongoIndexInitializer(mongoTemplate, environment);
        initializer.run(args);

        verify(mongoTemplate, atLeastOnce()).indexOps(any(Class.class));
    }

    @Test
    void run_whenAutoIndexCreationIsTrueAndNonProd_skipsProgrammaticIndexCreation() {
        when(environment.getProperty("spring.data.mongodb.auto-index-creation", "true")).thenReturn("true");
        when(environment.matchesProfiles("prod")).thenReturn(false);

        MongoIndexInitializer initializer = new MongoIndexInitializer(mongoTemplate, environment);
        initializer.run(args);

        verify(mongoTemplate, never()).indexOps(any(Class.class));
    }
}
