package com.fintech.banking.coreapi.account.infrastructure.adapter.out.persistence;

import java.util.Optional;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.fintech.banking.coreapi.account.application.port.out.AccountNumberGeneratorPort;
import com.fintech.banking.coreapi.account.infrastructure.adapter.out.persistence.repository.SpringDataAccountRepository;
import com.fintech.banking.coreapi.account.infrastructure.adapter.out.persistence.repository.SpringDataAvailableAccountNumberRepository;

/**
 * Implementation of the {@link AccountNumberGeneratorPort} that retrieves
 * numbers from the pre-generated pool in the database.
 */
@Component
public class PostgresAccountNumberGeneratorAdapter implements AccountNumberGeneratorPort {

	private final SpringDataAvailableAccountNumberRepository poolRepository;
	private final SpringDataAccountRepository accountRepository;
	private final boolean isH2Database;

	/**
	 * Constructs the adapter and dynamically detects if H2 is being used.
	 *
	 * @param poolRepository
	 *            repository for available numbers
	 * @param accountRepository
	 *            repository for accounts
	 * @param env
	 *            spring environment properties
	 */
	public PostgresAccountNumberGeneratorAdapter(SpringDataAvailableAccountNumberRepository poolRepository,
			SpringDataAccountRepository accountRepository, Environment env) {
		this.poolRepository = poolRepository;
		this.accountRepository = accountRepository;
		String driver = env.getProperty("spring.datasource.driver-class-name", "");
		String url = env.getProperty("spring.datasource.url", "");
		this.isH2Database = driver.toLowerCase().contains("h2") || url.toLowerCase().contains("h2");
	}

	@Override
	public String getNextAvailableNumber() {
		Optional<String> pooledNumber;

		if (isH2Database) {
			// Two-step process for H2 database tests to avoid syntax error on RETURNING
			pooledNumber = poolRepository.getAvailableNumberWithLock();
			if (pooledNumber.isPresent()) {
				poolRepository.deleteById(pooledNumber.get());
			}
		} else {
			// High-performance single-step atomic query for PostgreSQL
			pooledNumber = poolRepository.popAvailableNumberPostgres();
		}

		if (pooledNumber.isPresent()) {
			return pooledNumber.get();
		}

		// Fallback: if pool is empty, generate on the fly
		String number;
		do {
			number = com.fintech.banking.coreapi.account.domain.util.AccountNumberGenerator
					.generateRandom10DigitNumber();
		} while (accountRepository.existsByAccountNumber(number) || poolRepository.existsById(number));
		return number;
	}
}
