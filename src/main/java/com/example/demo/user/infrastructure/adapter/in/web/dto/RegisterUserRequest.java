package com.example.demo.user.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request payload to register a new user.
 *
 * @param name
 *            the user name
 * @param surname
 *            the user surname
 */
@Schema(description = "Request payload to register a new user")
public record RegisterUserRequest(
		@NotBlank(message = "Name is required") @Schema(description = "The first name of the user", example = "John") String name,
		@NotBlank(message = "Surname is required") @Schema(description = "The last name of the user", example = "Doe") String surname) {
}
