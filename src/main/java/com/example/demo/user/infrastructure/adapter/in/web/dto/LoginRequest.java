package com.example.demo.user.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for user login requests.
 *
 * @param email
 *            the user's email
 * @param password
 *            the user's password
 */
@Schema(description = "Request payload for user login")
public record LoginRequest(
		@Schema(description = "User's email address", example = "alice@example.com") @NotBlank @Email String email,
		@Schema(description = "User's raw password", example = "Password123!") @NotBlank String password) {
}
