package com.example.demo.infrastructure.adapter.out.persistence.repository;

import com.example.demo.infrastructure.adapter.out.persistence.entity.TransactionJpaEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataTransactionRepository extends JpaRepository<TransactionJpaEntity, UUID> {
    Page<TransactionJpaEntity> findBySourceAccountNumberOrTargetAccountNumberOrderByTimestampDesc(
            String sourceAccountNumber, String targetAccountNumber, Pageable pageable);
}