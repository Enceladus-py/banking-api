package com.fintech.banking.coreapi.common.infrastructure.security;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {

	private JwtService jwtService;
	private UserDetails userDetails;

	@BeforeEach
	void setUp() {
		jwtService = new JwtService();
		ReflectionTestUtils.setField(jwtService, "secretKey",
				"404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
		ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86400000L);

		userDetails = new User("user@example.com", "password", Collections.emptyList());
	}

	@Test
	void shouldGenerateAndValidateToken() {
		String token = jwtService.generateToken(userDetails);

		assertNotNull(token);

		String extractedUsername = jwtService.extractUsername(token);
		assertEquals("user@example.com", extractedUsername);

		assertTrue(jwtService.isTokenValid(token, userDetails));
	}

	@Test
	void shouldInvalidateTokenForDifferentUser() {
		String token = jwtService.generateToken(userDetails);

		UserDetails differentUser = new User("other@example.com", "password", Collections.emptyList());

		assertFalse(jwtService.isTokenValid(token, differentUser));
	}

	@Test
	void shouldGenerateAndValidateRefreshToken() {
		ReflectionTestUtils.setField(jwtService, "refreshExpiration", 604800000L);
		String token = jwtService.generateRefreshToken(userDetails);

		assertNotNull(token);
		String extractedUsername = jwtService.extractUsername(token);
		assertEquals("user@example.com", extractedUsername);
		assertTrue(jwtService.isTokenValid(token, userDetails));
	}

	@Test
	void shouldThrowExpiredJwtExceptionWhenTokenExpired() {
		ReflectionTestUtils.setField(jwtService, "jwtExpiration", -10000L);
		String token = jwtService.generateToken(userDetails);

		assertThrows(io.jsonwebtoken.ExpiredJwtException.class, () -> jwtService.isTokenValid(token, userDetails));
	}
}
