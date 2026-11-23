package com.viraj.dmabackend.common.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Set;

public final class PaginationUtils {

    public static final int MAX_PAGE_SIZE = 100;
    public static final int MAX_KEYWORD_LENGTH = 100;

    public static final Set<String> DEFAULT_DISALLOWED_PROPERTIES = Set.of(
            "password",
            "tokensRevokedAt",
            "tokenHash",
            "refreshToken"
    );

    private PaginationUtils() {
    }

    public static Pageable clampPageable(Pageable pageable) {
        return clampPageable(pageable, null);
    }

    public static Pageable clampPageable(Pageable pageable, Set<String> allowedProperties) {
        if (pageable == null || pageable.isUnpaged()) {
            return PageRequest.of(0, MAX_PAGE_SIZE);
        }
        int pageNumber = Math.max(0, pageable.getPageNumber());
        int pageSize = Math.max(1, Math.min(pageable.getPageSize(), MAX_PAGE_SIZE));
        Sort sort = sanitizeSort(pageable.getSort(), allowedProperties);
        return PageRequest.of(pageNumber, pageSize, sort);
    }

    public static Pageable clampAndValidateSort(Pageable pageable, Set<String> allowedProperties) {
        return clampPageable(pageable, allowedProperties);
    }

    public static Sort sanitizeSort(Sort sort, Set<String> allowedProperties) {
        if (sort == null || sort.isUnsorted()) {
            return Sort.unsorted();
        }
        List<Sort.Order> validOrders = sort.stream()
                .filter(order -> {
                    String prop = order.getProperty();
                    if (prop == null || prop.isBlank()) {
                        return false;
                    }
                    if (DEFAULT_DISALLOWED_PROPERTIES.contains(prop)) {
                        return false;
                    }
                    if (allowedProperties != null && !allowedProperties.isEmpty()) {
                        return allowedProperties.contains(prop);
                    }
                    return true;
                })
                .toList();
        return validOrders.isEmpty() ? Sort.unsorted() : Sort.by(validOrders);
    }

    public static String clampKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        String trimmed = keyword.trim();
        if (trimmed.length() > MAX_KEYWORD_LENGTH) {
            trimmed = trimmed.substring(0, MAX_KEYWORD_LENGTH);
        }

        if (trimmed.contains("\\")) {
            trimmed = trimmed.replace("\\", "");
        }

        long quoteCount = trimmed.chars().filter(ch -> ch == '"').count();
        if (quoteCount % 2 != 0) {
            trimmed = trimmed.replace("\"", "");
        }

        trimmed = trimmed.trim();
        String[] tokens = trimmed.split("\\s+");
        boolean hasPositive = false;
        for (String token : tokens) {
            if (!token.isEmpty() && !token.startsWith("-")) {
                hasPositive = true;
                break;
            }
        }
        if (!hasPositive && tokens.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (String token : tokens) {
                String stripped = token.replaceAll("^-+", "");
                if (!stripped.isEmpty()) {
                    if (sb.length() > 0) {
                        sb.append(" ");
                    }
                    sb.append(stripped);
                }
            }
            trimmed = sb.toString();
        }

        return trimmed.trim();
    }
}
