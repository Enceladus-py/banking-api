package com.example.demo.account.application.port.in;

import com.example.demo.account.domain.model.Account;
import com.example.demo.common.application.port.in.dto.PageRequest;
import com.example.demo.common.application.port.in.dto.PageResult;

/**
 * Inbound port interface for retrieving multiple accounts.
 */
public interface GetAccountsUseCase {

	/**
	 * Retrieves all accounts owned by the given user.
	 *
	 * @param userId
	 *            the user ID
	 * @param pageRequest
	 *            the pagination parameters
	 * @return a page of accounts owned by the user
	 */
	PageResult<Account> getAccountsByUserId(String userId, PageRequest pageRequest);
}
