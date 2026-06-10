package com.fintech.banking.coreapi.user.application.port.in;

import com.fintech.banking.coreapi.user.domain.model.User;

/**
 * Inbound port interface for retrieving user details.
 */
public interface GetUserUseCase {
	/**
	 * Retrieves user details by user ID.
	 *
	 * @param userId
	 *            the user ID
	 * @return the user details
	 */
	User getUserById(String userId);
}
