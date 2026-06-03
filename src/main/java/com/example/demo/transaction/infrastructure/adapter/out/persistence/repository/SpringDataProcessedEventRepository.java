package com.example.demo.transaction.infrastructure.adapter.out.persistence.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.transaction.infrastructure.adapter.out.persistence.entity.ProcessedEventJpaEntity;

public interface SpringDataProcessedEventRepository extends JpaRepository<ProcessedEventJpaEntity, UUID> {
}
