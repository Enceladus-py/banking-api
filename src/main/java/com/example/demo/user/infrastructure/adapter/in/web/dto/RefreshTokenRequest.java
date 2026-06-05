package com.example.demo.user.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for user refresh token request.
 *
 * @param refreshToken
 *            the refresh token provided by the client
 */
@Schema(description = "Request payload for refreshing the JWT token")
public record RefreshTokenRequest(
		@Schema(description = "JWT Refresh Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI...") @NotBlank(message = "Refresh token cannot be blank") String refreshToken) {
}
