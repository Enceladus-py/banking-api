package com.example.demo.account.application.port.in;

import java.util.Optional;

import com.example.demo.account.domain.model.Account;

/**
 * Inbound port that exposes account read and mutation operations to other
 * modules (e.g. {@code transaction}). This is the only sanctioned way for
 * external modules to interact with {@link Account} aggregates; they must never
 * reach into the account module's outbound {@code AccountRepository} port
 * directly.
 */
public interface AccountOperationsPort {

	/**
	 * Persists the given account state and returns the saved instance.
	 *
	 * @param account
	 *            the account state to persist
	 * @return the saved account instance
	 */
	Account save(Account account);

	/**
	 * Looks up an account by its unique account number.
	 *
	 * @param accountNumber
	 *            the unique account number
	 * @return an Optional containing the account if found, or empty
	 */
	Optional<Account> findByAccountNumber(String accountNumber);

	/**
	 * Loads an account and acquires a pessimistic write lock on it, ensuring
	 * exclusive access for the duration of the current transaction. Use this
	 * whenever the caller intends to mutate the account balance.
	 *
	 * @param accountNumber
	 *            the account number to load and lock
	 * @return an Optional containing the locked account if found, or empty
	 */
	Optional<Account> lockAndLoad(String accountNumber);
}
