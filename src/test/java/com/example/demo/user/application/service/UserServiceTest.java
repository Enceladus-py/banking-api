package com.example.demo.user.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.common.domain.exception.EntityNotFoundException;
import com.example.demo.user.application.port.in.RegisterUserUseCase.RegisterUserCommand;
import com.example.demo.user.application.port.out.UserRepository;
import com.example.demo.user.domain.model.Profile;
import com.example.demo.user.domain.model.User;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private UserService userService;

	@Test
	void shouldRegisterUserSuccessfully() {
		RegisterUserCommand command = new RegisterUserCommand("alice@example.com", "password", "Alice", "Smith");
		when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.empty());
		when(passwordEncoder.encode("password")).thenReturn("encoded_password");
		when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

		User result = userService.registerUser(command);

		assertNotNull(result.getId());
		assertEquals("alice@example.com", result.getEmail());
		assertEquals("encoded_password", result.getPassword());
		assertEquals("Alice", result.getProfile().getName());
		assertEquals("Smith", result.getProfile().getSurname());
		verify(userRepository, times(1)).save(any(User.class));
	}

	@Test
	void shouldThrowExceptionWhenEmailAlreadyInUse() {
		RegisterUserCommand command = new RegisterUserCommand("alice@example.com", "password", "Alice", "Smith");
		User existingUser = User.reconstitute("USER-1", "alice@example.com", "pwd", Profile.createNew("A", "B"), 1L);
		when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(existingUser));

		assertThrows(IllegalArgumentException.class, () -> userService.registerUser(command));
	}

	@Test
	void shouldGetUserById() {
		User existingUser = User.reconstitute("USER-1", "alice@example.com", "pwd", Profile.createNew("Alice", "Smith"),
				1L);
		when(userRepository.findById("USER-1")).thenReturn(Optional.of(existingUser));

		User result = userService.getUserById("USER-1");

		assertEquals(existingUser, result);
	}

	@Test
	void shouldThrowEntityNotFoundExceptionWhenUserNotFound() {
		when(userRepository.findById("NON-EXISTENT")).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> userService.getUserById("NON-EXISTENT"));
	}
}
