package com.example.demo.account.application.service;

import java.util.UUID;

import com.example.demo.account.application.port.in.CreateAccountUseCase;
import com.example.demo.account.application.port.out.AccountRepository;
import com.example.demo.account.domain.model.Account;
import com.example.demo.common.application.annotation.TransactionalUseCase;
import com.example.demo.common.domain.exception.EntityNotFoundException;
import com.example.demo.user.application.port.out.UserRepository;

@TransactionalUseCase
public class CreateAccountService implements CreateAccountUseCase {

	private final AccountRepository accountRepository;
	private final UserRepository userRepository;

	public CreateAccountService(AccountRepository accountRepository, UserRepository userRepository) {
		this.accountRepository = accountRepository;
		this.userRepository = userRepository;
	}

	@Override
	public Account createAccount(CreateAccountCommand command) {
		userRepository.findById(command.requesterId()).orElseThrow(() -> new EntityNotFoundException("User not found"));
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
