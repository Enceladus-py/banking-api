package com.example.demo.account.application.service;

import com.example.demo.account.application.port.in.GetAccountUseCase;
import com.example.demo.account.application.port.out.AccountRepository;
import com.example.demo.account.domain.model.Account;
import com.example.demo.common.application.annotation.UseCase;
import com.example.demo.common.domain.exception.EntityNotFoundException;

/**
 * Service class that implements the use case for retrieving account details.
 */
@UseCase
public class AccountQueryService implements GetAccountUseCase {

	private final AccountRepository accountRepository;

	/**
	 * Constructs a new AccountQueryService with the specified repository.
	 *
	 * @param accountRepository
	 *            the account repository outbound port
	 */
	public AccountQueryService(AccountRepository accountRepository) {
		this.accountRepository = accountRepository;
	}

	@Override
	public Account getAccount(String accountNumber, String requesterId) {
		Account account = accountRepository.findByAccountNumber(accountNumber)
				.orElseThrow(() -> new EntityNotFoundException("Account not found"));

		if (!account.isOwnedBy(requesterId)) {
			throw new SecurityException("You are not authorized to view this account");
		}

		return account;
	}
}
