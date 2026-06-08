package com.example.demo.common.infrastructure.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import jakarta.servlet.ServletException;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

	@Mock
	private JwtService jwtService;

	@Mock
	private UserDetailsService userDetailsService;

	@InjectMocks
	private JwtAuthenticationFilter jwtAuthenticationFilter;

	@BeforeEach
	void setUp() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void shouldSkipFilterWhenNoAuthHeader() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain filterChain = new MockFilterChain();

		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		assertNull(SecurityContextHolder.getContext().getAuthentication());
	}

	@Test
	void shouldAuthenticateWhenValidTokenProvided() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "Bearer valid.jwt.token");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain filterChain = new MockFilterChain();

		UserDetails userDetails = new User("alice@example.com", "pwd", Collections.emptyList());

		when(jwtService.extractUsername("valid.jwt.token")).thenReturn("alice@example.com");
		when(userDetailsService.loadUserByUsername("alice@example.com")).thenReturn(userDetails);
		when(jwtService.isTokenValid("valid.jwt.token", userDetails)).thenReturn(true);

		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		assertNotNull(SecurityContextHolder.getContext().getAuthentication());
		assertEquals("alice@example.com", SecurityContextHolder.getContext().getAuthentication().getName());
	}

	@Test
	void shouldSkipAuthenticationWhenTokenIsInvalid() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "Bearer invalid.jwt.token");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain filterChain = new MockFilterChain();

		when(jwtService.extractUsername("invalid.jwt.token")).thenThrow(new RuntimeException("Parsing failed"));

		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		assertNull(SecurityContextHolder.getContext().getAuthentication());
	}

	@Test
	void shouldSkipFilterWhenAuthHeaderDoesNotStartWithBearer() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "Basic abc");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain filterChain = new MockFilterChain();

		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		assertNull(SecurityContextHolder.getContext().getAuthentication());
	}

	@Test
	void shouldNotAuthenticateWhenUsernameIsNull() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "Bearer valid.jwt.token");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain filterChain = new MockFilterChain();

		when(jwtService.extractUsername("valid.jwt.token")).thenReturn(null);

		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		assertNull(SecurityContextHolder.getContext().getAuthentication());
	}

	@Test
	void shouldNotAuthenticateWhenAlreadyAuthenticated() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "Bearer valid.jwt.token");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain filterChain = new MockFilterChain();

		UserDetails userDetails = new User("alice@example.com", "pwd", Collections.emptyList());
		UsernamePasswordAuthenticationToken existingAuth = new UsernamePasswordAuthenticationToken(userDetails, null,
				userDetails.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(existingAuth);

		when(jwtService.extractUsername("valid.jwt.token")).thenReturn("alice@example.com");

		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		assertEquals(existingAuth, SecurityContextHolder.getContext().getAuthentication());
		verify(userDetailsService, never()).loadUserByUsername(anyString());
	}

	@Test
	void shouldNotAuthenticateWhenTokenValidationFails() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "Bearer invalid.jwt.token");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain filterChain = new MockFilterChain();

		UserDetails userDetails = new User("alice@example.com", "pwd", Collections.emptyList());

		when(jwtService.extractUsername("invalid.jwt.token")).thenReturn("alice@example.com");
		when(userDetailsService.loadUserByUsername("alice@example.com")).thenReturn(userDetails);
		when(jwtService.isTokenValid("invalid.jwt.token", userDetails)).thenReturn(false);

		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		assertNull(SecurityContextHolder.getContext().getAuthentication());
	}
}
