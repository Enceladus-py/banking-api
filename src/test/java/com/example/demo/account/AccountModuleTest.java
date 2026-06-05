package com.example.demo.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.example.demo.account.application.port.in.CreateAccountUseCase;
import com.example.demo.account.application.port.in.CreateAccountUseCase.CreateAccountCommand;
import com.example.demo.account.application.port.in.GetAccountUseCase;
import com.example.demo.account.domain.model.Account;
import com.example.demo.common.domain.exception.EntityNotFoundException;
import com.example.demo.user.application.port.in.GetUserUseCase;
import com.example.demo.user.domain.model.User;

/**
 * Spring Modulith integration test for the {@code account} module.
 *
 * <p>
 * Bootstraps only the beans belonging to the {@code account} module plus its
 * declared dependencies. The cross-module {@link GetUserUseCase} dependency is
 * satisfied via a Mockito bean to keep the test isolated from the {@code user}
 * module's persistence layer.
 */
@ApplicationModuleTest
@org.springframework.context.annotation.ComponentScan(basePackageClasses = AccountModuleTest.class, includeFilters = @org.springframework.context.annotation.ComponentScan.Filter(type = org.springframework.context.annotation.FilterType.ANNOTATION, classes = com.example.demo.common.application.annotation.UseCase.class))
class AccountModuleTest {

	@Autowired
	CreateAccountUseCase createAccountUseCase;

	@Autowired
	GetAccountUseCase getAccountUseCase;

	@MockitoBean
	GetUserUseCase getUserUseCase;

	@Test
	void shouldCreateAndRetrieveAccount() {
		String userId = "user-integration-01";
		when(getUserUseCase.getUserById(userId)).thenReturn(new User(userId, "alice@example.com", "pwd", new com.example.demo.user.domain.model.Profile("Alice", "Smith"), 1L));

		Account created = createAccountUseCase.createAccount(new CreateAccountCommand(userId));

		assertThat(created).isNotNull();
		assertThat(created.getOwnerId()).isEqualTo(userId);
		assertThat(created.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
		assertThat(created.getAccountNumber()).hasSize(10);
	}

	@Test
	void shouldEnforceOwnershipOnGetAccount() {
		String ownerId = "user-integration-02";
		when(getUserUseCase.getUserById(ownerId)).thenReturn(new User(ownerId, "bob@example.com", "pwd", new com.example.demo.user.domain.model.Profile("Bob", "Jones"), 1L));

		Account created = createAccountUseCase.createAccount(new CreateAccountCommand(ownerId));

		assertThatThrownBy(() -> getAccountUseCase.getAccount(created.getAccountNumber(), "attacker-id"))
				.isInstanceOf(SecurityException.class).hasMessage("You are not authorized to view this account");
	}

	@Test
	void shouldThrowEntityNotFoundWhenUserDoesNotExist() {
		String missingUserId = "ghost-user";
		when(getUserUseCase.getUserById(missingUserId))
				.thenThrow(new EntityNotFoundException("User not found with ID: " + missingUserId));

		assertThatThrownBy(() -> createAccountUseCase.createAccount(new CreateAccountCommand(missingUserId)))
				.isInstanceOf(EntityNotFoundException.class);
	}

	@Test
	void shouldThrowEntityNotFoundWhenAccountDoesNotExist() {
		assertThatThrownBy(() -> getAccountUseCase.getAccount("NOTEXIST00", "any-user"))
				.isInstanceOf(EntityNotFoundException.class).hasMessage("Account not found");
	}
}
