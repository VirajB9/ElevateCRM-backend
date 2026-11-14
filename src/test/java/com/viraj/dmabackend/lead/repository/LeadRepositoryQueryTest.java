package com.viraj.dmabackend.lead.repository;

import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.repository.Query;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class LeadRepositoryQueryTest {

    @Test
    void testLeadRepositoryQueriesAreValidBsonAndIncludeStatusPredicate() {
        for (Method method : LeadRepository.class.getDeclaredMethods()) {
            Query query = method.getAnnotation(Query.class);
            if (query != null) {
                String queryString = query.value();
                // Replace parameters with dummy values
                String dummyQuery = queryString
                        .replace("?0", "\"TEST_PARAM_0\"")
                        .replace("?1", "[\"DELETED\", \"LOST\"]")
                        .replace("?2", "\"TEST_PARAM_2\"");

                Document doc = Document.parse(dummyQuery);
                assertNotNull(doc, "Query must parse to valid BSON document: " + queryString);

                if (method.getName().contains("Status")) {
                    assertTrue(queryString.contains("status"),
                            "Method " + method.getName() + " taking status must include 'status' in @Query: " + queryString);
                    assertTrue(doc.containsKey("status"),
                            "Parsed document must contain 'status' key: " + queryString);
                }

                for (int i = 0; i < method.getParameterCount(); i++) {
                    Class<?> paramType = method.getParameterTypes()[i];
                    if (com.viraj.dmabackend.lead.enums.LeadStatus.class.isAssignableFrom(paramType) ||
                            (java.util.Collection.class.isAssignableFrom(paramType) && method.getName().contains("Status"))) {
                        assertTrue(queryString.contains("?" + i),
                                "Method " + method.getName() + " taking status at param " + i + " must bind ?" + i + " in @Query: " + queryString);
                    }
                }
            }
        }
    }
}
