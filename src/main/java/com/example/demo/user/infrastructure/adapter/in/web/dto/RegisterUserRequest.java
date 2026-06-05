package com.example.demo.user.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload to register a new user.
 *
 * @param email
 *            the user email
 * @param password
 *            the user password
 * @param name
 *            the user name
 * @param surname
 *            the user surname
 */
@Schema(description = "Request payload to register a new user")
public record RegisterUserRequest(
		@NotBlank(message = "Email is required") @Email(message = "Email must be valid") @Schema(description = "The email address of the user", example = "john.doe@example.com") String email,
		@NotBlank(message = "Password is required") @Size(min = 8, message = "Password must be at least 8 characters") @Schema(description = "The password for the user account", example = "SecurePass123!") String password,
		@NotBlank(message = "Name is required") @Schema(description = "The first name of the user", example = "John") String name,
		@NotBlank(message = "Surname is required") @Schema(description = "The last name of the user", example = "Doe") String surname) {
}
