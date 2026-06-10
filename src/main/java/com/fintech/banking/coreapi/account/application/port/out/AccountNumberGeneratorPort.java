package com.fintech.banking.coreapi.account.application.port.out;

/**
 * Outbound port for generating or retrieving unique account numbers.
 */
public interface AccountNumberGeneratorPort {

	/**
	 * Retrieves the next available account number.
	 *
	 * @return a guaranteed unique account number
	 */
	String getNextAvailableNumber();
}
