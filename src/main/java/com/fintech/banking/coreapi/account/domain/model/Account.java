package com.fintech.banking.coreapi.account.domain.model;

import java.math.BigDecimal;

import com.fintech.banking.coreapi.account.domain.exception.InsufficientFundsException;

import lombok.Getter;

/**
 * Domain model representing a bank account.
 */
@Getter
public class Account {
	private String id;
	private String ownerId;
	private String accountNumber;
	private BigDecimal balance;
	private boolean isNew;

	/**
	 * Factory method to create a new bank account with business validations.
	 *
	 * @param ownerId
	 *            the user ID of the owner
	 * @param accountNumber
	 *            the unique 10-digit account number
	 * @return the newly created Account
	 */
	public static Account createNew(String ownerId, String accountNumber) {
		if (accountNumber == null || accountNumber.length() != 10) {
			throw new IllegalArgumentException("Account number must be exactly 10 characters");
		}
		if (ownerId == null || ownerId.trim().isBlank()) {
			throw new IllegalArgumentException("Owner ID cannot be blank");
		}
		Account account = new Account(java.util.UUID.randomUUID().toString(), ownerId, accountNumber, BigDecimal.ZERO);
		account.isNew = true;
		return account;
	}

	/**
	 * Reconstitutes an Account from persistent storage.
	 *
	 * @param id
	 *            the unique account ID
	 * @param ownerId
	 *            the owner's user ID
	 * @param accountNumber
	 *            the 10-digit account number
	 * @param balance
	 *            the account balance
	 * @return the reconstituted Account
	 */
	public static Account reconstitute(String id, String ownerId, String accountNumber, BigDecimal balance) {
		return new Account(id, ownerId, accountNumber, balance);
	}

	/**
	 * Private constructor used by static factory methods.
	 *
	 * @param id
	 *            the unique account ID
	 * @param ownerId
	 *            the owner's user ID
	 * @param accountNumber
	 *            the 10-digit account number
	 * @param balance
	 *            the account balance
	 */
	private Account(String id, String ownerId, String accountNumber, BigDecimal balance) {
		this.id = id;
		this.ownerId = ownerId;
		this.accountNumber = accountNumber;
		this.balance = balance;
		this.isNew = false;
	}

	/**
	 * Returns true if this is a newly created account that hasn't been persisted
	 * yet.
	 *
	 * @return true if new
	 */
	public boolean isNewAccount() {
		return isNew;
	}

	/**
	 * Checks if this account is owned by the specified user.
	 *
	 * @param userId
	 *            the user ID to check
	 * @return true if owned by the user, false otherwise
	 */
	public boolean isOwnedBy(String userId) {
		return this.ownerId.equals(userId);
	}

	/**
	 * Deposits an amount into the account.
	 *
	 * @param amount
	 *            the deposit amount
	 */
	public void deposit(BigDecimal amount) {
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("Deposit amount must be greater than zero");
		}
		this.balance = this.balance.add(amount);
	}

	/**
	 * Withdraws an amount from the account.
	 *
	 * @param amount
	 *            the withdrawal amount
	 */
	public void withdraw(BigDecimal amount) {
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("Withdrawal amount must be greater than zero");
		}
		if (this.balance.compareTo(amount) < 0) {
			throw new InsufficientFundsException("Insufficient funds");
		}
		this.balance = this.balance.subtract(amount);
	}
}
