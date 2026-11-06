package com.viraj.dmabackend.project.entity;

import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.index.CompoundIndex;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectEntityTest {

    @Test
    void projectEntity_HasClientStatusCompoundIndex() {
        CompoundIndex[] indexes = Project.class.getAnnotationsByType(CompoundIndex.class);

        boolean hasClientStatusIndex = Arrays.stream(indexes)
                .anyMatch(idx -> "idx_client_status".equals(idx.name()) && "{'clientId': 1, 'status': 1}".equals(idx.def()));

        assertThat(hasClientStatusIndex).isTrue();
    }
}
