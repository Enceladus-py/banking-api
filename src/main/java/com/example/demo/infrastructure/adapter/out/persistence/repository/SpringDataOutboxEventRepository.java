package com.example.demo.infrastructure.adapter.out.persistence.repository;

import com.example.demo.infrastructure.adapter.out.persistence.entity.OutboxEventJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

import com.example.demo.infrastructure.adapter.out.persistence.entity.OutboxStatus;

public interface SpringDataOutboxEventRepository extends JpaRepository<OutboxEventJpaEntity, UUID> {
    List<OutboxEventJpaEntity> findByStatusOrderByCreatedAtAsc(OutboxStatus status);
}
