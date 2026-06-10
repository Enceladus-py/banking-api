package com.fintech.banking.coreapi.user.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Request DTO for updating user profile information.
 *
 * @param mobileNumber
 *            the new mobile number (optional)
 * @param address
 *            the new address (optional)
 */
@Schema(description = "Request payload for updating user profile")
public record ProfileUpdateRequest(
		@Schema(description = "User's mobile number", example = "+1234567890", requiredMode = Schema.RequiredMode.NOT_REQUIRED) String mobileNumber,
		@Schema(description = "User's physical address", example = "123 Main St, City, Country", requiredMode = Schema.RequiredMode.NOT_REQUIRED) String address) {
}
