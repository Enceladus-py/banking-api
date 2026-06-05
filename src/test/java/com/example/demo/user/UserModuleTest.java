package com.example.demo.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.example.demo.common.domain.exception.EntityNotFoundException;
import com.example.demo.user.application.port.in.GetUserUseCase;
import com.example.demo.user.application.port.in.RegisterUserUseCase;
import com.example.demo.user.application.port.in.RegisterUserUseCase.RegisterUserCommand;
import com.example.demo.user.domain.model.User;

@ApplicationModuleTest
@org.springframework.context.annotation.ComponentScan(basePackageClasses = UserModuleTest.class, includeFilters = @org.springframework.context.annotation.ComponentScan.Filter(type = org.springframework.context.annotation.FilterType.ANNOTATION, classes = com.example.demo.common.application.annotation.UseCase.class))
class UserModuleTest {

	@Autowired
	RegisterUserUseCase registerUserUseCase;

	@Autowired
	GetUserUseCase getUserUseCase;

	@MockitoBean
	PasswordEncoder passwordEncoder;

	@MockitoBean
	org.springframework.security.authentication.AuthenticationManager authenticationManager;

	@MockitoBean
	com.example.demo.common.security.JwtService jwtService;

	@Test
	void shouldRegisterAndRetrieveUser() {
		org.mockito.Mockito.when(passwordEncoder.encode("pwd123")).thenReturn("hashed");

		RegisterUserCommand command = new RegisterUserCommand("charlie@example.com", "pwd123", "Charlie", "Brown");

		User registered = registerUserUseCase.registerUser(command);

		assertThat(registered).isNotNull();
		assertThat(registered.getId()).isNotBlank();
		assertThat(registered.getEmail()).isEqualTo("charlie@example.com");
		assertThat(registered.getProfile().getName()).isEqualTo("Charlie");
		assertThat(registered.getProfile().getSurname()).isEqualTo("Brown");
	}

	@Test
	void shouldRetrieveUserByIdAfterRegistration() {
		org.mockito.Mockito.when(passwordEncoder.encode("pwd123")).thenReturn("hashed");

		User registered = registerUserUseCase
				.registerUser(new RegisterUserCommand("diana@example.com", "pwd123", "Diana", "Prince"));

		User retrieved = getUserUseCase.getUserById(registered.getId());

		assertThat(retrieved.getId()).isEqualTo(registered.getId());
		assertThat(retrieved.getProfile().getName()).isEqualTo("Diana");
	}

	@Test
	void shouldThrowEntityNotFoundForUnknownUserId() {
		String missingId = "00000000-0000-0000-0000-000000000099";
		assertThatThrownBy(() -> getUserUseCase.getUserById(missingId)).isInstanceOf(EntityNotFoundException.class)
				.hasMessageContaining(missingId);
	}

	@Test
	void shouldTrimWhitespaceInNameAndSurname() {
		org.mockito.Mockito.when(passwordEncoder.encode("pwd123")).thenReturn("hashed");
		User registered = registerUserUseCase
				.registerUser(new RegisterUserCommand("eve@example.com", "pwd123", "  Eve  ", "  Walker  "));

		assertThat(registered.getProfile().getName()).isEqualTo("Eve");
		assertThat(registered.getProfile().getSurname()).isEqualTo("Walker");
	}
}
