package com.viraj.dmabackend.client.repository;

import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.repository.Query;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class ClientRepositoryQueryTest {

    @Test
    void testClientRepositoryGstQueriesAreValidBsonWithoutDuplicateKeys() {
        for (Method method : ClientRepository.class.getDeclaredMethods()) {
            Query query = method.getAnnotation(Query.class);
            if (query != null) {
                String queryString = query.value();
                // Replace parameters with dummy values
                String dummyQuery = queryString
                        .replace("?0", "\"TEST_GST_123\"")
                        .replace("?1", "\"TEST_PARAM_1\"")
                        .replace("?2", "\"TEST_PARAM_2\"");

                Document doc = Document.parse(dummyQuery);
                assertNotNull(doc);

                if (queryString.contains("gstNumber")) {
                    Object gstCondition = doc.get("gstNumber");
                    assertNotNull(gstCondition, "gstNumber field must exist in query: " + queryString);
                    assertTrue(gstCondition instanceof Document, "gstNumber should be a subdocument with $eq and $type: " + queryString);
                    Document gstDoc = (Document) gstCondition;
                    assertTrue(gstDoc.containsKey("$eq"), "gstNumber query must retain $eq: " + queryString);
                    assertTrue(gstDoc.containsKey("$type"), "gstNumber query must retain $type: " + queryString);
                    assertEquals("TEST_GST_123", gstDoc.getString("$eq"));
                    assertEquals("string", gstDoc.getString("$type"));
                }
            }
        }
    }
}
