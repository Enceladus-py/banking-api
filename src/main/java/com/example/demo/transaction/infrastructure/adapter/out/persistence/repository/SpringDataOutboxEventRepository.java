package com.example.demo.transaction.infrastructure.adapter.out.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.transaction.infrastructure.adapter.out.persistence.entity.OutboxEventJpaEntity;
import com.example.demo.transaction.infrastructure.adapter.out.persistence.entity.OutboxStatus;

/**
 * Spring Data JPA repository for {@link OutboxEventJpaEntity}.
 */
public interface SpringDataOutboxEventRepository extends JpaRepository<OutboxEventJpaEntity, UUID> {
	/**
	 * Finds outbox events by status ordered by creation timestamp ascending.
	 *
	 * @param status
	 *            the outbox event dispatch status
	 * @return the list of outbox events matching status
	 */
	List<OutboxEventJpaEntity> findByStatusOrderByCreatedAtAsc(OutboxStatus status);
}
