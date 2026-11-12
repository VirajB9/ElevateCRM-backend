package com.viraj.dmabackend.common.util;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaginationUtilsTest {

    @Test
    void clampPageable_NullReturnsDefaultBoundedPageable() {
        Pageable result = PaginationUtils.clampPageable(null);
        assertNotNull(result);
        assertEquals(0, result.getPageNumber());
        assertEquals(100, result.getPageSize());
    }

    @Test
    void clampPageable_UnpagedReturnsBoundedPageable() {
        Pageable result = PaginationUtils.clampPageable(Pageable.unpaged());
        assertNotNull(result);
        assertEquals(0, result.getPageNumber());
        assertEquals(100, result.getPageSize());
    }

    @Test
    void clampPageable_NormalPageSizeKeptUnchanged() {
        Pageable normal = PageRequest.of(2, 25, Sort.by("name").descending());
        Pageable result = PaginationUtils.clampPageable(normal);
        assertEquals(2, result.getPageNumber());
        assertEquals(25, result.getPageSize());
        assertEquals(normal.getSort(), result.getSort());
    }

    @Test
    void clampPageable_ExcessivePageSizeClampedToMax() {
        Pageable excessive = PageRequest.of(3, 500, Sort.by("createdAt").ascending());
        Pageable result = PaginationUtils.clampPageable(excessive);
        assertEquals(3, result.getPageNumber());
        assertEquals(100, result.getPageSize());
        assertEquals(excessive.getSort(), result.getSort());
    }

    @Test
    void clampKeyword_NullReturnsNull() {
        org.junit.jupiter.api.Assertions.assertNull(PaginationUtils.clampKeyword(null));
    }

    @Test
    void clampKeyword_NormalStringTrimmed() {
        assertEquals("test keyword", PaginationUtils.clampKeyword("   test keyword   "));
    }

    @Test
    void clampKeyword_ExcessiveLengthClampedTo100() {
        String longInput = "a".repeat(150);
        String clamped = PaginationUtils.clampKeyword(longInput);
        assertEquals(100, clamped.length());
        assertEquals("a".repeat(100), clamped);
    }
}
