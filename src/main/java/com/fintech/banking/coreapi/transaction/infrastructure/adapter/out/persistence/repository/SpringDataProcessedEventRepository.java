package com.fintech.banking.coreapi.transaction.infrastructure.adapter.out.persistence.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fintech.banking.coreapi.transaction.infrastructure.adapter.out.persistence.entity.ProcessedEventJpaEntity;

/**
 * Spring Data JPA repository for {@link ProcessedEventJpaEntity}.
 */
public interface SpringDataProcessedEventRepository extends JpaRepository<ProcessedEventJpaEntity, UUID> {
}
