package com.fintech.banking.coreapi.account.infrastructure.adapter.out.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.fintech.banking.coreapi.account.infrastructure.adapter.out.persistence.entity.AvailableAccountNumberJpaEntity;

/**
 * Spring Data JPA repository for available account numbers.
 */
@Repository
public interface SpringDataAvailableAccountNumberRepository
		extends
			JpaRepository<AvailableAccountNumberJpaEntity, String> {

	/**
	 * Gets an available account number from the pool with a row lock, skipping
	 * locked rows.
	 *
	 * @return an optional containing the account number, or empty if pool is empty
	 */
	@Query(value = "SELECT account_number FROM available_account_numbers LIMIT 1 FOR UPDATE SKIP LOCKED", nativeQuery = true)
	Optional<String> getAvailableNumberWithLock();

	/**
	 * Pops an available account number from the pool using a single atomic
	 * PostgreSQL query.
	 *
	 * @return an optional containing the account number, or empty if pool is empty
	 */
	@Query(value = """
			DELETE FROM available_account_numbers
			WHERE account_number = (
			    SELECT account_number FROM available_account_numbers
			    LIMIT 1 FOR UPDATE SKIP LOCKED
			) RETURNING account_number
			""", nativeQuery = true)
	Optional<String> popAvailableNumberPostgres();

	/**
	 * Finds account numbers that already exist in the pool from a given set of
	 * candidates.
	 *
	 * @param accountNumbers
	 *            the candidate account numbers
	 * @return a set of account numbers that already exist
	 */
	@Query("SELECT a.accountNumber FROM AvailableAccountNumberJpaEntity a WHERE a.accountNumber IN :accountNumbers")
	java.util.Set<String> findExistingAccountNumbers(
			@org.springframework.data.repository.query.Param("accountNumbers") java.util.Collection<String> accountNumbers);
}
