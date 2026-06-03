package com.example.demo.account.application.service;

import java.util.Optional;

import com.example.demo.account.application.port.in.AccountOperationsPort;
import com.example.demo.account.application.port.out.AccountRepository;
import com.example.demo.account.domain.model.Account;
import com.example.demo.common.application.annotation.UseCase;

/**
 * Implements {@link AccountOperationsPort}, delegating to the internal
 * {@link AccountRepository} outbound port. External modules (e.g.
 * {@code transaction}) depend solely on the {@link AccountOperationsPort}
 * interface and are therefore decoupled from the persistence implementation.
 */
@UseCase
public class AccountOperationsService implements AccountOperationsPort {

	private final AccountRepository accountRepository;

	/**
	 * Constructs a new AccountOperationsService with the specified repository.
	 *
	 * @param accountRepository
	 *            the account repository outbound port
	 */
	public AccountOperationsService(AccountRepository accountRepository) {
		this.accountRepository = accountRepository;
	}

	@Override
	public Account save(Account account) {
		return accountRepository.save(account);
	}

	@Override
	public Optional<Account> findByAccountNumber(String accountNumber) {
		return accountRepository.findByAccountNumber(accountNumber);
	}

	@Override
	public Optional<Account> lockAndLoad(String accountNumber) {
		return accountRepository.lockAndLoad(accountNumber);
	}
}
