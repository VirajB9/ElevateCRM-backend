package com.viraj.dmabackend.client.entity;

import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.index.CompoundIndex;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class ClientEntityTest {

    @Test
    void clientEntity_HasCompoundIndexes() {
        CompoundIndex[] indexes = Client.class.getAnnotationsByType(CompoundIndex.class);

        boolean hasStatusDeletedIndex = Arrays.stream(indexes)
                .anyMatch(idx -> "idx_client_status_deleted".equals(idx.name())
                        && "{'status': 1, 'deleted': 1}".equals(idx.def()));

        boolean hasDeletedCreatedAt = Arrays.stream(indexes)
                .anyMatch(idx -> "idx_client_deleted_created_at".equals(idx.name())
                        && "{'deleted': 1, 'createdAt': -1}".equals(idx.def()));

        assertThat(hasStatusDeletedIndex).isTrue();
        assertThat(hasDeletedCreatedAt).isTrue();
    }
}
