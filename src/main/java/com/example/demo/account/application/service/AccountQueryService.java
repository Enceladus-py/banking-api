package com.example.demo.account.application.service;

import com.example.demo.account.application.port.in.GetAccountUseCase;
import com.example.demo.account.application.port.out.AccountRepository;
import com.example.demo.account.domain.model.Account;
import com.example.demo.common.application.annotation.UseCase;
import com.example.demo.common.domain.exception.EntityNotFoundException;

@UseCase
public class AccountQueryService implements GetAccountUseCase {

	private final AccountRepository accountRepository;

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
