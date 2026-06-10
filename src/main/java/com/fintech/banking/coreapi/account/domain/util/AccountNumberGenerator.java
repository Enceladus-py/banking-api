package com.fintech.banking.coreapi.account.domain.util;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Utility class for generating random account numbers.
 */
public final class AccountNumberGenerator {

	private AccountNumberGenerator() {
		// Prevent instantiation
	}

	/**
	 * Generates a completely random 10-digit number.
	 *
	 * @return a 10-digit numeric string
	 */
	public static String generateRandom10DigitNumber() {
		long min = 1000000000L;
		long max = 9999999999L;
		long randomNum = ThreadLocalRandom.current().nextLong(min, max + 1);
		return String.valueOf(randomNum);
	}
}
