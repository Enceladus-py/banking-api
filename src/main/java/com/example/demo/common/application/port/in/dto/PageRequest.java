package com.example.demo.common.application.port.in.dto;

/**
 * Record representing a request for a paginated subset of results.
 *
 * @param pageNumber
 *            the 0-based page number requested
 * @param pageSize
 *            the number of elements per page
 */
public record PageRequest(int pageNumber, int pageSize) {
	/**
	 * Constructor with validations for page index and size limits.
	 *
	 * @param pageNumber
	 *            the 0-based page index
	 * @param pageSize
	 *            the page size
	 */
	public PageRequest {
		if (pageNumber < 0)
			throw new IllegalArgumentException("Page number cannot be less than zero");
		if (pageSize < 1)
			throw new IllegalArgumentException("Page size must not be less than one");
	}
}
