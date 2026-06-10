package com.fintech.banking.coreapi.common.application.port.in.dto;

import java.util.List;

/**
 * Record representing a paginated container of elements.
 *
 * @param <T>
 *            the type of element stored in the page content
 * @param content
 *            the list of elements for this page
 * @param pageNumber
 *            the current 0-based page number
 * @param pageSize
 *            the configured size of the page
 * @param totalElements
 *            the total count of elements across all pages
 * @param totalPages
 *            the total count of pages available
 */
public record PageResult<T>(List<T> content, int pageNumber, int pageSize, long totalElements, int totalPages) {
}
