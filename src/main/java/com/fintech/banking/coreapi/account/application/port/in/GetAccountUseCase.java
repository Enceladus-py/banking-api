package com.fintech.banking.coreapi.account.application.port.in;

import com.fintech.banking.coreapi.account.domain.model.Account;

/**
 * Inbound port interface for retrieving bank account details.
 */
public interface GetAccountUseCase {
	/**
	 * Retrieves an account using the account number and checks access
	 * authorization.
	 *
	 * @param accountNumber
	 *            the account number
	 * @param requesterId
	 *            the user ID requesting access
	 * @return the bank account details
	 */
	Account getAccount(String accountNumber, String requesterId);
}
