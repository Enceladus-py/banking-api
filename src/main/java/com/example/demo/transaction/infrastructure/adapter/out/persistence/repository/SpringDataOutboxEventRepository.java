package com.example.demo.transaction.infrastructure.adapter.out.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.transaction.infrastructure.adapter.out.persistence.entity.OutboxEventJpaEntity;
import com.example.demo.transaction.infrastructure.adapter.out.persistence.entity.OutboxStatus;

public interface SpringDataOutboxEventRepository extends JpaRepository<OutboxEventJpaEntity, UUID> {
	List<OutboxEventJpaEntity> findByStatusOrderByCreatedAtAsc(OutboxStatus status);
}
