package com.example.demo.infrastructure.adapter.out.persistence.repository;

import com.example.demo.infrastructure.adapter.out.persistence.entity.TransactionJpaEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

import com.example.demo.domain.model.TransactionRecord.TransactionStatus;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataTransactionRepository extends JpaRepository<TransactionJpaEntity, UUID> {
    Page<TransactionJpaEntity> findBySourceAccountNumberOrTargetAccountNumberOrderByTimestampDesc(
            String sourceAccountNumber, String targetAccountNumber, Pageable pageable);

    @Modifying
    @Query("UPDATE TransactionJpaEntity t SET t.status = :status, t.failureReason = :failureReason WHERE t.id = :id")
    void updateStatus(@Param("id") UUID id, @Param("status") TransactionStatus status, @Param("failureReason") String failureReason);
}