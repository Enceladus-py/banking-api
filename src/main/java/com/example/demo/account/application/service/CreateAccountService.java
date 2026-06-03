package com.example.demo.account.application.service;

import java.util.UUID;

import com.example.demo.account.application.port.in.CreateAccountUseCase;
import com.example.demo.account.application.port.out.AccountRepository;
import com.example.demo.account.domain.model.Account;
import com.example.demo.common.application.annotation.TransactionalUseCase;
import com.example.demo.user.application.port.in.GetUserUseCase;

/**
 * Service class that implements the use case for creating bank accounts.
 */
@TransactionalUseCase
public class CreateAccountService implements CreateAccountUseCase {

	private final AccountRepository accountRepository;
	private final GetUserUseCase getUserUseCase;

	/**
	 * Constructs a new CreateAccountService with the specified ports.
	 *
	 * @param accountRepository
	 *            the account repository outbound port
	 * @param getUserUseCase
	 *            the user retrieval inbound port
	 */
	public CreateAccountService(AccountRepository accountRepository, GetUserUseCase getUserUseCase) {
		this.accountRepository = accountRepository;
		this.getUserUseCase = getUserUseCase;
	}

	@Override
	public Account createAccount(CreateAccountCommand command) {
		// Verifies the user exists; throws EntityNotFoundException if not found
		getUserUseCase.getUserById(command.requesterId());
		String uniqueAccountNumber = generateUniqueAccountNumber();
		Account account = Account.createNew(command.requesterId(), uniqueAccountNumber);
		return accountRepository.save(account);
	}

	private String generateUniqueAccountNumber() {
		String accountNumber;
		do {
			accountNumber = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
		} while (accountRepository.findByAccountNumber(accountNumber).isPresent());

		return accountNumber;
	}
}
