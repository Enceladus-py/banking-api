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

import com.example.demo.common.domain.exception.EntityNotFoundException;
import com.example.demo.user.application.port.in.RegisterUserUseCase.RegisterUserCommand;
import com.example.demo.user.application.port.out.UserRepository;
import com.example.demo.user.domain.model.User;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserService userService;

	@Test
	void shouldRegisterUserSuccessfully() {
		RegisterUserCommand command = new RegisterUserCommand("123e4567-e89b-12d3-a456-426614174004", "John", "Doe");
		when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

		User result = userService.registerUser(command);

		assertNotNull(result.getId());
		assertEquals("John", result.getName());
		assertEquals("Doe", result.getSurname());
		verify(userRepository, times(1)).save(any(User.class));
	}

	@Test
	void shouldGetUserById() {
		User existingUser = new User("USER-1", "Alice", "Smith", 1L);
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
