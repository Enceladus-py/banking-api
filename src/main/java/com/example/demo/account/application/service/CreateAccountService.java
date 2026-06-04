package com.example.demo.account.application.service;

import com.example.demo.account.application.port.in.CreateAccountUseCase;
import com.example.demo.account.application.port.out.AccountNumberGeneratorPort;
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
	private final AccountNumberGeneratorPort accountNumberGeneratorPort;

	/**
	 * Constructs a new CreateAccountService with the specified ports.
	 *
	 * @param accountRepository
	 *            the account repository outbound port
	 * @param getUserUseCase
	 *            the user retrieval inbound port
	 * @param accountNumberGeneratorPort
	 *            the port to generate unique account numbers
	 */
	public CreateAccountService(AccountRepository accountRepository, GetUserUseCase getUserUseCase,
			AccountNumberGeneratorPort accountNumberGeneratorPort) {
		this.accountRepository = accountRepository;
		this.getUserUseCase = getUserUseCase;
		this.accountNumberGeneratorPort = accountNumberGeneratorPort;
	}

	@Override
	public Account createAccount(CreateAccountCommand command) {
		// Verifies the user exists; throws EntityNotFoundException if not found
		getUserUseCase.getUserById(command.requesterId());
		String uniqueAccountNumber = accountNumberGeneratorPort.getNextAvailableNumber();
		Account account = Account.createNew(command.requesterId(), uniqueAccountNumber);
		return accountRepository.save(account);
	}
}
