package com.example.demo.account.infrastructure.adapter.out.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.account.infrastructure.adapter.out.persistence.entity.AccountJpaEntity;

import jakarta.persistence.LockModeType;

/**
 * Spring Data JPA repository for {@link AccountJpaEntity}.
 */
public interface SpringDataAccountRepository extends JpaRepository<AccountJpaEntity, UUID> {
	/**
	 * Finds an account entity by its unique account number.
	 *
	 * @param accountNumber
	 *            the account number
	 * @return an Optional containing the account entity if found, or empty
	 */
	Optional<AccountJpaEntity> findByAccountNumber(String accountNumber);

	/**
	 * Loads an account entity and locks it using a pessimistic write lock.
	 *
	 * @param accountNumber
	 *            the account number
	 * @return an Optional containing the locked account entity if found, or empty
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT a FROM AccountJpaEntity a WHERE a.accountNumber = :accountNumber")
	Optional<AccountJpaEntity> findByAccountNumberForWrite(@Param("accountNumber") String accountNumber);

	/**
	 * Finds all account entities owned by the specified user.
	 *
	 * @param ownerId
	 *            the user ID
	 * @param pageable
	 *            the pagination information
	 * @return a page of account entities owned by the user
	 */
	Page<AccountJpaEntity> findByOwnerId(String ownerId, Pageable pageable);
}
