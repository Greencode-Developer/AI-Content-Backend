package com.ai_content.controller.common;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Generic pagination response wrapper.
 *
 * @param content       current page items
 * @param page          current page number (0-indexed)
 * @param size          page size requested
 * @param totalElements total number of items across all pages
 * @param totalPages    total number of pages
 * @param hasNext       whether a next page exists
 */
public record PagedResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static <E, T> PagedResponse<T> from(Page<E> pageResult, Function<E, T> mapper) {
        return new PagedResponse<>(
                pageResult.getContent().stream().map(mapper).toList(),
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.hasNext()
        );
    }
}
