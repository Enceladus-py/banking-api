package com.example.demo.user.infrastructure.adapter.in.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.demo.user.application.port.out.UserRepository;
import com.example.demo.user.domain.model.Profile;
import com.example.demo.user.domain.model.User;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private CustomUserDetailsService customUserDetailsService;

	@Test
	void shouldLoadUserByUsernameSuccessfully() {
		User user = User.reconstitute("USER-1", "alice@example.com", "encodedPwd", Profile.createNew("Alice", "Smith"),
				1L);
		when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

		UserDetails userDetails = customUserDetailsService.loadUserByUsername("alice@example.com");

		assertNotNull(userDetails);
		assertEquals("alice@example.com", userDetails.getUsername());
		assertEquals("encodedPwd", userDetails.getPassword());
		assertTrue(userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("USER")));
	}

	@Test
	void shouldThrowUsernameNotFoundExceptionWhenUserNotFound() {
		when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

		assertThrows(UsernameNotFoundException.class,
				() -> customUserDetailsService.loadUserByUsername("missing@example.com"));
	}
}
