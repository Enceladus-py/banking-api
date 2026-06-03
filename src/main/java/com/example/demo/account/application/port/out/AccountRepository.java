package com.example.demo.account.application.port.out;

import java.util.Optional;

import com.example.demo.account.domain.model.Account;

public interface AccountRepository {
	Account save(Account account);

	Optional<Account> findByAccountNumber(String accountNumber);

	/**
	 * Loads an account and acquires a pessimistic write lock on it, ensuring
	 * exclusive access for the duration of the current transaction. Use this
	 * whenever the caller intends to mutate the account balance.
	 */
	Optional<Account> lockAndLoad(String accountNumber);
}
