package com.viraj.dmabackend.lead.entity;

import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.index.CompoundIndex;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class LeadEntityTest {

    @Test
    void leadEntity_HasCompoundIndexes() {
        CompoundIndex[] indexes = Lead.class.getAnnotationsByType(CompoundIndex.class);

        boolean hasAssignedUserStatusIndex = Arrays.stream(indexes)
                .anyMatch(idx -> "idx_assigned_user_status".equals(idx.name())
                        && "{'assignedUserId': 1, 'status': 1}".equals(idx.def()));

        boolean hasSourceStatusIndex = Arrays.stream(indexes)
                .anyMatch(idx -> "idx_source_status".equals(idx.name())
                        && "{'source': 1, 'status': 1}".equals(idx.def()));

        assertThat(hasAssignedUserStatusIndex).isTrue();
        assertThat(hasSourceStatusIndex).isTrue();
    }
}
