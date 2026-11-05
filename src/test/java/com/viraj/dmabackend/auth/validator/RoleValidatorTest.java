package com.viraj.dmabackend.auth.validator;

import com.viraj.dmabackend.auth.exception.DuplicatePermissionException;
import com.viraj.dmabackend.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoleValidatorTest {

    private final RoleValidator roleValidator = new RoleValidator();

    @Test
    void validatePermissionIds_whenNullOrEmpty_throwsBadRequestException() {
        assertThatThrownBy(() -> roleValidator.validatePermissionIds(null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Permission IDs list cannot be empty");

        assertThatThrownBy(() -> roleValidator.validatePermissionIds(Collections.emptyList()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Permission IDs list cannot be empty");
    }

    @Test
    void validatePermissionIds_whenContainsNullOrBlankElement_throwsBadRequestException() {
        List<String> listWithNull = new ArrayList<>();
        listWithNull.add("perm-1");
        listWithNull.add(null);

        assertThatThrownBy(() -> roleValidator.validatePermissionIds(listWithNull))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Permission ID cannot be null or blank");

        assertThatThrownBy(() -> roleValidator.validatePermissionIds(List.of("perm-1", "   ")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Permission ID cannot be null or blank");
    }

    @Test
    void validatePermissionIds_whenContainsDuplicates_throwsDuplicatePermissionException() {
        assertThatThrownBy(() -> roleValidator.validatePermissionIds(List.of("perm-1", "perm-1")))
                .isInstanceOf(DuplicatePermissionException.class);
    }

    @Test
    void validatePermissionIds_whenValid_succeeds() {
        assertThatCode(() -> roleValidator.validatePermissionIds(List.of("perm-1", "perm-2")))
                .doesNotThrowAnyException();
    }

    @Test
    void validateDescription_whenNullOrBlank_throwsBadRequestException() {
        assertThatThrownBy(() -> roleValidator.validateDescription(null))
                .isInstanceOf(BadRequestException.class);

        assertThatThrownBy(() -> roleValidator.validateDescription("   "))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void validateDescription_whenValid_succeeds() {
        assertThatCode(() -> roleValidator.validateDescription("Valid role description"))
                .doesNotThrowAnyException();
    }
}
