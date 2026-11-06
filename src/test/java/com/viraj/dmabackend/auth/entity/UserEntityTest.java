package com.viraj.dmabackend.auth.entity;

import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.index.CompoundIndex;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class UserEntityTest {

    @Test
    void userEntity_HasRoleStatusCompoundIndex() {
        CompoundIndex[] indexes = User.class.getAnnotationsByType(CompoundIndex.class);

        boolean hasRoleStatusIndex = Arrays.stream(indexes)
                .anyMatch(idx -> "idx_role_status".equals(idx.name())
                        && "{'roleId': 1, 'status': 1}".equals(idx.def()));

        assertThat(hasRoleStatusIndex).isTrue();
    }
}
