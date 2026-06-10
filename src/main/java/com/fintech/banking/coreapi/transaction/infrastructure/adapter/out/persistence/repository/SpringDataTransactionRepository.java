package com.fintech.banking.coreapi.transaction.infrastructure.adapter.out.persistence.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.fintech.banking.coreapi.transaction.infrastructure.adapter.out.persistence.entity.TransactionJpaEntity;

/**
 * Spring Data JPA repository for {@link TransactionJpaEntity}.
 */
public interface SpringDataTransactionRepository extends JpaRepository<TransactionJpaEntity, UUID> {
	/**
	 * Finds transactions involving the specified source or target account numbers.
	 *
	 * @param sourceAccountNumber
	 *            the source account number
	 * @param targetAccountNumber
	 *            the target account number
	 * @param pageable
	 *            pagination details
	 * @return page of transaction entities
	 */
	Page<TransactionJpaEntity> findBySourceAccountNumberOrTargetAccountNumberOrderByTimestampDesc(
			String sourceAccountNumber, String targetAccountNumber, Pageable pageable);
}
