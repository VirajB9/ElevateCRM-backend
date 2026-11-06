package com.viraj.dmabackend.auth.entity;

import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.index.Indexed;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

class PermissionEntityTest {

    @Test
    void permissionType_HasUniqueIndex() throws NoSuchFieldException {
        Field field = Permission.class.getDeclaredField("permissionType");
        Indexed indexed = field.getAnnotation(Indexed.class);

        assertThat(indexed).isNotNull();
        assertThat(indexed.unique()).isTrue();
    }
}
