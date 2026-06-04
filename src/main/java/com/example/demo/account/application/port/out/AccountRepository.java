package com.example.demo.account.application.port.out;

import java.util.Optional;

import com.example.demo.account.domain.model.Account;
import com.example.demo.common.application.port.in.dto.PageRequest;
import com.example.demo.common.application.port.in.dto.PageResult;

/**
 * Outbound port interface for account persistence operations.
 */
public interface AccountRepository {
	/**
	 * Saves the given account entity.
	 *
	 * @param account
	 *            the account to save
	 * @return the saved account
	 */
	Account save(Account account);

	/**
	 * Finds an account by its unique account number.
	 *
	 * @param accountNumber
	 *            the account number
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

	/**
	 * Finds a page of accounts owned by the specified user.
	 *
	 * @param ownerId
	 *            the ID of the user
	 * @param pageRequest
	 *            the pagination information
	 * @return a page of accounts owned by the user
	 */
	PageResult<Account> findByOwnerId(String ownerId, PageRequest pageRequest);
}
