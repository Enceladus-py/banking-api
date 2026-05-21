package com.example.demo.infrastructure.adapter.out.persistence;

import com.example.demo.application.port.out.TransactionRecordRepository;
import com.example.demo.domain.model.TransactionRecord;
import com.example.demo.infrastructure.adapter.out.persistence.entity.TransactionJpaEntity;
import com.example.demo.infrastructure.adapter.out.persistence.repository.SpringDataTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PostgresTransactionAdapter implements TransactionRecordRepository {

    private final SpringDataTransactionRepository repository;

    @Override
    public void save(TransactionRecord transaction) {
        TransactionJpaEntity jpaEntity = toJpaEntity(transaction);
        repository.saveAndFlush(jpaEntity);
    }

    @Override
    public List<TransactionRecord> findByAccountNumber(String accountNumber) {
        return repository
                .findBySourceAccountNumberOrTargetAccountNumberOrderByTimestampDesc(accountNumber, accountNumber)
                .stream()
                .map(this::toDomainModel)
                .collect(Collectors.toList());
    }

    private TransactionJpaEntity toJpaEntity(TransactionRecord domain) {
        return TransactionJpaEntity.builder()
                .id(UUID.fromString(domain.getId()))
                .sourceAccountNumber(domain.getSourceAccountNumber())
                .targetAccountNumber(domain.getTargetAccountNumber())
                .amount(domain.getAmount())
                .type(domain.getType())
                .timestamp(domain.getTimestamp())
                .build();
    }

    private TransactionRecord toDomainModel(TransactionJpaEntity entity) {
        return new TransactionRecord(
                entity.getId().toString(),
                entity.getSourceAccountNumber(),
                entity.getTargetAccountNumber(),
                entity.getAmount(),
                entity.getType(),
                entity.getTimestamp());
    }
}