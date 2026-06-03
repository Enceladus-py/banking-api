package com.example.demo.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;

import com.example.demo.common.domain.exception.EntityNotFoundException;
import com.example.demo.user.application.port.in.GetUserUseCase;
import com.example.demo.user.application.port.in.RegisterUserUseCase;
import com.example.demo.user.application.port.in.RegisterUserUseCase.RegisterUserCommand;
import com.example.demo.user.domain.model.User;

/**
 * Spring Modulith integration test for the {@code user} module.
 *
 * <p>
 * Bootstraps only the beans belonging to the {@code user} module. The
 * {@code user} module has no cross-module runtime dependencies, so no mock
 * beans are required.
 */
@ApplicationModuleTest
@org.springframework.context.annotation.ComponentScan(basePackageClasses = UserModuleTest.class, includeFilters = @org.springframework.context.annotation.ComponentScan.Filter(type = org.springframework.context.annotation.FilterType.ANNOTATION, classes = com.example.demo.common.application.annotation.UseCase.class))
class UserModuleTest {

	@Autowired
	RegisterUserUseCase registerUserUseCase;

	@Autowired
	GetUserUseCase getUserUseCase;

	@Test
	void shouldRegisterAndRetrieveUser() {
		RegisterUserCommand command = new RegisterUserCommand("Charlie", "Brown");

		User registered = registerUserUseCase.registerUser(command);

		assertThat(registered).isNotNull();
		assertThat(registered.getId()).isNotBlank();
		assertThat(registered.getName()).isEqualTo("Charlie");
		assertThat(registered.getSurname()).isEqualTo("Brown");
	}

	@Test
	void shouldRetrieveUserByIdAfterRegistration() {
		User registered = registerUserUseCase.registerUser(new RegisterUserCommand("Diana", "Prince"));

		User retrieved = getUserUseCase.getUserById(registered.getId());

		assertThat(retrieved.getId()).isEqualTo(registered.getId());
		assertThat(retrieved.getName()).isEqualTo("Diana");
	}

	@Test
	void shouldThrowEntityNotFoundForUnknownUserId() {
		String missingId = "00000000-0000-0000-0000-000000000099";
		assertThatThrownBy(() -> getUserUseCase.getUserById(missingId)).isInstanceOf(EntityNotFoundException.class)
				.hasMessageContaining(missingId);
	}

	@Test
	void shouldTrimWhitespaceInNameAndSurname() {
		User registered = registerUserUseCase.registerUser(new RegisterUserCommand("  Eve  ", "  Walker  "));

		assertThat(registered.getName()).isEqualTo("Eve");
		assertThat(registered.getSurname()).isEqualTo("Walker");
	}
}
