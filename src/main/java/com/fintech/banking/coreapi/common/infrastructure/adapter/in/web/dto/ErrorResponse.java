package com.fintech.banking.coreapi.common.infrastructure.adapter.in.web.dto;

import java.time.Instant;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Standard error response structure for API exceptions.
 *
 * @param timestamp
 *            the time the error occurred
 * @param message
 *            a descriptive error message
 * @param errors
 *            optional list of specific field errors (e.g. for validation
 *            failures)
 */
@Schema(description = "Standard error response structure")
public record ErrorResponse(
		@Schema(description = "Timestamp when the error occurred", example = "2026-06-08T10:15:30") Instant timestamp,
		@Schema(description = "Error message description", example = "Validation failed") String message,
		@Schema(description = "List of specific field errors if applicable") List<String> errors) {

	/**
	 * Creates an error response without specific field errors.
	 *
	 * @param timestamp
	 *            the time the error occurred
	 * @param message
	 *            a descriptive error message
	 */
	public ErrorResponse(Instant timestamp, String message) {
		this(timestamp, message, null);
	}
}
