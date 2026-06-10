package com.fintech.banking.coreapi.account.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fintech.banking.coreapi.account.application.port.in.CreateAccountUseCase.CreateAccountCommand;
import com.fintech.banking.coreapi.account.application.port.out.AccountNumberGeneratorPort;
import com.fintech.banking.coreapi.account.application.port.out.AccountRepository;
import com.fintech.banking.coreapi.account.domain.model.Account;
import com.fintech.banking.coreapi.common.domain.exception.EntityNotFoundException;
import com.fintech.banking.coreapi.user.application.port.in.GetUserUseCase;
import com.fintech.banking.coreapi.user.domain.model.User;

@ExtendWith(MockitoExtension.class)
class CreateAccountServiceTest {

	@Mock
	private GetUserUseCase getUserUseCase;

	@Mock
	private AccountRepository accountRepository;

	@Mock
	private AccountNumberGeneratorPort accountNumberGeneratorPort;

	private CreateAccountService createAccountService;

	@BeforeEach
	void setUp() {
		createAccountService = new CreateAccountService(accountRepository, getUserUseCase, accountNumberGeneratorPort);
	}

	@Test
	void shouldCreateAccountAndSaveToRepository() {
		String requesterId = "USER-123";
		CreateAccountCommand command = new CreateAccountCommand(requesterId);

		when(getUserUseCase.getUserById(requesterId)).thenReturn(User.reconstitute(requesterId, "user@example.com",
				"pwd", com.fintech.banking.coreapi.user.domain.model.Profile.createNew("Berat", "Dalsuna"), 1L));
		when(accountNumberGeneratorPort.getNextAvailableNumber()).thenReturn("1234567890");

		Account mockSavedAccount = Account.reconstitute("uuid-123", requesterId, "1234567890", BigDecimal.ZERO);
		when(accountRepository.save(any(Account.class))).thenReturn(mockSavedAccount);

		Account result = createAccountService.createAccount(command);

		assertNotNull(result);
		assertEquals("1234567890", result.getAccountNumber());
		verify(accountRepository, times(1)).save(any(Account.class));
		verify(accountNumberGeneratorPort, times(1)).getNextAvailableNumber();
	}

	@Test
	void shouldThrowEntityNotFoundExceptionWhenUserNotFound() {
		String requesterId = "NON-EXISTENT";
		CreateAccountCommand command = new CreateAccountCommand(requesterId);

		when(getUserUseCase.getUserById(requesterId)).thenThrow(
				new com.fintech.banking.coreapi.common.domain.exception.EntityNotFoundException("User not found"));

		assertThrows(EntityNotFoundException.class, () -> createAccountService.createAccount(command));
		verify(accountRepository, never()).save(any(Account.class));
		verify(accountNumberGeneratorPort, never()).getNextAvailableNumber();
	}
}
