package com.fintech.banking.coreapi.account.application.service;

import com.fintech.banking.coreapi.account.application.port.in.GetAccountUseCase;
import com.fintech.banking.coreapi.account.application.port.in.GetAccountsUseCase;
import com.fintech.banking.coreapi.account.application.port.out.AccountRepository;
import com.fintech.banking.coreapi.account.domain.model.Account;
import com.fintech.banking.coreapi.common.application.annotation.UseCase;
import com.fintech.banking.coreapi.common.application.port.in.dto.PageRequest;
import com.fintech.banking.coreapi.common.application.port.in.dto.PageResult;
import com.fintech.banking.coreapi.common.domain.exception.AccessDeniedException;
import com.fintech.banking.coreapi.common.domain.exception.EntityNotFoundException;

/**
 * Service class that implements the use case for retrieving account details.
 */
@UseCase
public class AccountQueryService implements GetAccountUseCase, GetAccountsUseCase {

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
			throw new AccessDeniedException("You are not authorized to view this account");
		}

		return account;
	}

	@Override
	public PageResult<Account> getAccountsByUserId(String userId, PageRequest pageRequest) {
		return accountRepository.findByOwnerId(userId, pageRequest);
	}
}
