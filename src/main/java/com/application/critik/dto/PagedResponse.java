package com.application.critik.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Generic wrapper for paginated responses.
 * Provides pagination metadata along with the content.
 * 
 * @param <T> Type of content in the page
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagedResponse<T> {

    /** List of items in the current page */
    private List<T> content;

    /** Current page number (0-indexed) */
    private int page;

    /** Number of items per page */
    private int size;

    /** Total number of items across all pages */
    private long totalElements;

    /** Total number of pages */
    private int totalPages;

    /** Whether this is the last page */
    private boolean last;

    /** Whether this is the first page */
    private boolean first;
}
