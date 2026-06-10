package com.fintech.banking.coreapi.transaction.infrastructure.adapter.out.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import com.fintech.banking.coreapi.transaction.infrastructure.adapter.out.persistence.entity.OutboxEventJpaEntity;
import com.fintech.banking.coreapi.transaction.infrastructure.adapter.out.persistence.entity.OutboxStatus;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

/**
 * Spring Data JPA repository for {@link OutboxEventJpaEntity}.
 */
public interface SpringDataOutboxEventRepository extends JpaRepository<OutboxEventJpaEntity, UUID> {
	/**
	 * Finds outbox events by status ordered by creation timestamp ascending,
	 * locking the rows.
	 *
	 * @param status
	 *            the outbox event dispatch status
	 * @param pageable
	 *            the pagination information
	 * @return the list of outbox events matching status
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2")})
	@Query("SELECT e FROM OutboxEventJpaEntity e WHERE e.status = :status ORDER BY e.createdAt ASC")
	List<OutboxEventJpaEntity> findByStatusOrderByCreatedAtAscWithLock(@Param("status") OutboxStatus status,
			Pageable pageable);
}
