package com.example.demo.account.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.account.application.port.out.AccountRepository;
import com.example.demo.account.domain.model.Account;
import com.example.demo.common.domain.exception.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class AccountQueryServiceTest {

	@Mock
	private AccountRepository accountRepository;

	@InjectMocks
	private AccountQueryService accountQueryService;

	@Test
	void shouldReturnAccountWhenAuthorized() {
		Account account = Account.createNew("USER-1", "1234567890");
		when(accountRepository.findByAccountNumber("1234567890")).thenReturn(Optional.of(account));

		Account result = accountQueryService.getAccount("1234567890", "USER-1");

		assertEquals("1234567890", result.getAccountNumber());
		assertEquals("USER-1", result.getOwnerId());
	}

	@Test
	void shouldThrowSecurityExceptionWhenUnauthorized() {
		Account account = Account.createNew("USER-1", "1234567890");
		when(accountRepository.findByAccountNumber("1234567890")).thenReturn(Optional.of(account));

		assertThrows(SecurityException.class, () -> accountQueryService.getAccount("1234567890", "HACKER"));
	}

	@Test
	void shouldThrowEntityNotFoundExceptionWhenAccountDoesNotExist() {
		when(accountRepository.findByAccountNumber("1234567890")).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> accountQueryService.getAccount("1234567890", "USER-1"));
	}

	@Test
	void shouldReturnAccountsByUserId() {
		com.example.demo.common.application.port.in.dto.PageRequest pageRequest = new com.example.demo.common.application.port.in.dto.PageRequest(
				0, 10);
		com.example.demo.common.application.port.in.dto.PageResult<Account> mockResult = new com.example.demo.common.application.port.in.dto.PageResult<>(
				java.util.List.of(Account.createNew("USER-1", "1234567890")), 0, 10, 1, 1);
		when(accountRepository.findByOwnerId("USER-1", pageRequest)).thenReturn(mockResult);

		com.example.demo.common.application.port.in.dto.PageResult<Account> result = accountQueryService
				.getAccountsByUserId("USER-1", pageRequest);

		assertEquals(1, result.totalElements());
	}
}
