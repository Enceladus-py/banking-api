package com.fintech.banking.coreapi.user.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response payload representing a user.
 *
 * @param id
 *            the user ID
 * @param email
 *            the user email
 * @param name
 *            the user name
 * @param surname
 *            the user surname
 * @param mobileNumber
 *            the user mobile number
 * @param address
 *            the user address
 */
@Schema(description = "Response payload containing user profile details")
public record UserResponse(
		@Schema(description = "The unique identifier of the user", example = "123e4567-e89b-12d3-a456-426614174000") String id,
		@Schema(description = "The email address of the user", example = "john.doe@example.com") String email,
		@Schema(description = "The first name of the user", example = "John") String name,
		@Schema(description = "The last name of the user", example = "Doe") String surname,
		@Schema(description = "The mobile number of the user", example = "+1234567890", requiredMode = Schema.RequiredMode.NOT_REQUIRED) String mobileNumber,
		@Schema(description = "The address of the user", example = "123 Main St, City, Country", requiredMode = Schema.RequiredMode.NOT_REQUIRED) String address) {
}
