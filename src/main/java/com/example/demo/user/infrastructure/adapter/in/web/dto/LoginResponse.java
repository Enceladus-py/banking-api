package com.example.demo.user.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for user login response.
 *
 * @param accessToken
 *            the generated JWT access token
 * @param refreshToken
 *            the generated JWT refresh token
 */
@Schema(description = "Response payload containing the JWT tokens")
public record LoginResponse(
		@Schema(description = "JWT Access Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI...") String accessToken,
		@Schema(description = "JWT Refresh Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI...") String refreshToken) {
}
