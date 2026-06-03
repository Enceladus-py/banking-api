package com.example.demo.account.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.account.application.port.in.CreateAccountUseCase.CreateAccountCommand;
import com.example.demo.account.application.port.out.AccountRepository;
import com.example.demo.account.domain.model.Account;
import com.example.demo.common.domain.exception.EntityNotFoundException;
import com.example.demo.user.application.port.out.UserRepository;
import com.example.demo.user.domain.model.User;

@ExtendWith(MockitoExtension.class)
class CreateAccountServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private AccountRepository accountRepository;

	private CreateAccountService createAccountService;

	@BeforeEach
	void setUp() {
		createAccountService = new CreateAccountService(accountRepository, userRepository);
	}

	@Test
	void shouldCreateAccountAndSaveToRepository() {
		String requesterId = "USER-123";
		CreateAccountCommand command = new CreateAccountCommand(requesterId);

		when(userRepository.findById(requesterId))
				.thenReturn(Optional.of(new User(requesterId, "Berat", "Dalsuna", 1L)));

		Account mockSavedAccount = new Account("uuid-123", requesterId, "A1B2C3D4E5", BigDecimal.ZERO, 1L);
		when(accountRepository.save(any(Account.class))).thenReturn(mockSavedAccount);

		Account result = createAccountService.createAccount(command);

		assertNotNull(result);
		assertEquals("A1B2C3D4E5", result.getAccountNumber());
		verify(accountRepository, times(1)).save(any(Account.class));
	}

	@Test
	void shouldThrowEntityNotFoundExceptionWhenUserNotFound() {
		String requesterId = "NON-EXISTENT";
		CreateAccountCommand command = new CreateAccountCommand(requesterId);

		when(userRepository.findById(requesterId)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> createAccountService.createAccount(command));
		verify(accountRepository, never()).save(any(Account.class));
	}

	@Test
	void shouldRetryGeneratingAccountNumberWhenCollisionOccurs() {
		String requesterId = "USER-123";
		CreateAccountCommand command = new CreateAccountCommand(requesterId);

		when(userRepository.findById(requesterId))
				.thenReturn(Optional.of(new User(requesterId, "Berat", "Dalsuna", 1L)));

		// Simulate a collision on the first generated account number, then success on
		// the second
		when(accountRepository.findByAccountNumber(anyString()))
				.thenReturn(Optional.of(new Account("id", "owner", "ACC1", BigDecimal.ZERO, 1L))) // 1st try: collision
				.thenReturn(Optional.empty()); // 2nd try: available

		Account mockSavedAccount = new Account("uuid-123", requesterId, "A1B2C3D4E5", BigDecimal.ZERO, 1L);
		when(accountRepository.save(any(Account.class))).thenReturn(mockSavedAccount);

		createAccountService.createAccount(command);

		verify(accountRepository, times(2)).findByAccountNumber(anyString());
		verify(accountRepository, times(1)).save(any(Account.class));
	}
}
