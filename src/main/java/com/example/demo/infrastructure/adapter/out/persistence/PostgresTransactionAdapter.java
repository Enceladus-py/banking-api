package com.example.demo.infrastructure.adapter.out.persistence;

import com.example.demo.application.port.in.dto.PageResult;
import com.example.demo.application.port.out.TransactionRecordRepository;
import com.example.demo.domain.model.TransactionRecord;
import com.example.demo.infrastructure.adapter.out.persistence.entity.TransactionJpaEntity;
import com.example.demo.infrastructure.adapter.out.persistence.repository.SpringDataTransactionRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PostgresTransactionAdapter implements TransactionRecordRepository {

    private final SpringDataTransactionRepository repository;

    @Override
    public void save(TransactionRecord transaction) {
        TransactionJpaEntity jpaEntity = toJpaEntity(transaction);
        if (repository.existsById(jpaEntity.getId())) {
            repository.updateStatus(jpaEntity.getId(), jpaEntity.getStatus(), jpaEntity.getFailureReason());
        } else {
            repository.saveAndFlush(jpaEntity);
        }
    }

    @Override
    public Optional<TransactionRecord> findById(String id) {
        return repository.findById(UUID.fromString(id))
                .map(this::toDomainModel);
    }

    @Override
    public PageResult<TransactionRecord> findByAccountNumber(String accountNumber,
            com.example.demo.application.port.in.dto.PageRequest purePageRequest) {

        // 1. Translate core PageRequest to Spring Data Pageable
        Pageable springPageable = org.springframework.data.domain.PageRequest.of(
                purePageRequest.pageNumber(),
                purePageRequest.pageSize());

        // 2. Execute paginated query
        Page<TransactionJpaEntity> springPage = repository
                .findBySourceAccountNumberOrTargetAccountNumberOrderByTimestampDesc(accountNumber, accountNumber,
                        springPageable);

        // 3. Translate Spring Page back to core PageResult
        List<TransactionRecord> domainContent = springPage.getContent().stream()
                .map(this::toDomainModel)
                .collect(Collectors.toList());

        return new PageResult<>(
                domainContent,
                springPage.getNumber(),
                springPage.getSize(),
                springPage.getTotalElements(),
                springPage.getTotalPages());
    }

    private TransactionJpaEntity toJpaEntity(TransactionRecord domain) {
        return TransactionJpaEntity.builder()
                .id(UUID.fromString(domain.getId()))
                .sourceAccountNumber(domain.getSourceAccountNumber())
                .targetAccountNumber(domain.getTargetAccountNumber())
                .amount(domain.getAmount())
                .type(domain.getType())
                .timestamp(domain.getTimestamp())
                .status(domain.getStatus())
                .failureReason(domain.getFailureReason())
                .build();
    }

    private TransactionRecord toDomainModel(TransactionJpaEntity entity) {
        return new TransactionRecord(
                entity.getId().toString(),
                entity.getSourceAccountNumber(),
                entity.getTargetAccountNumber(),
                entity.getAmount(),
                entity.getType(),
                entity.getTimestamp(),
                entity.getStatus(),
                entity.getFailureReason());
    }
}