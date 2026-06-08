package com.example.demo.transaction.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.example.demo.common.application.port.in.dto.PageResult;
import com.example.demo.transaction.application.port.out.TransactionRecordRepository;
import com.example.demo.transaction.domain.model.TransactionRecord;
import com.example.demo.transaction.infrastructure.adapter.out.persistence.entity.TransactionJpaEntity;
import com.example.demo.transaction.infrastructure.adapter.out.persistence.repository.SpringDataTransactionRepository;

/**
 * Persistence adapter implementing TransactionRecordRepository for PostgreSQL
 * database.
 */
@Component
public class PostgresTransactionAdapter implements TransactionRecordRepository {

	private final SpringDataTransactionRepository repository;

	/**
	 * Constructs a new PostgresTransactionAdapter with the specified Spring Data
	 * repository.
	 *
	 * @param repository
	 *            the Spring Data JPA repository
	 */
	public PostgresTransactionAdapter(SpringDataTransactionRepository repository) {
		this.repository = repository;
	}

	@Override
	public void save(TransactionRecord transaction) {
		// isNew() on TransactionJpaEntity is driven by a @Transient flag:
		// new instances → INSERT; instances loaded from DB → UPDATE.
		// No extra SELECT (existsById) needed.
		repository.saveAndFlush(toJpaEntity(transaction));
	}

	@Override
	public Optional<TransactionRecord> findById(String id) {
		return repository.findById(UUID.fromString(id)).map(this::toDomainModel);
	}

	@Override
	public PageResult<TransactionRecord> findByAccountNumber(String accountNumber,
			com.example.demo.common.application.port.in.dto.PageRequest purePageRequest) {

		// 1. Translate core PageRequest to Spring Data Pageable
		Pageable springPageable = org.springframework.data.domain.PageRequest.of(purePageRequest.pageNumber(),
				purePageRequest.pageSize());

		// 2. Execute paginated query
		Page<TransactionJpaEntity> springPage = repository
				.findBySourceAccountNumberOrTargetAccountNumberOrderByTimestampDesc(accountNumber, accountNumber,
						springPageable);

		// 3. Translate Spring Page back to core PageResult
		List<TransactionRecord> domainContent = springPage.getContent().stream().map(this::toDomainModel)
				.collect(Collectors.toList());

		return new PageResult<>(domainContent, springPage.getNumber(), springPage.getSize(),
				springPage.getTotalElements(), springPage.getTotalPages());
	}

	private TransactionJpaEntity toJpaEntity(TransactionRecord domain) {
		boolean isNewEntity = domain.getStatus() == TransactionRecord.TransactionStatus.PENDING;
		return TransactionJpaEntity.builder().id(UUID.fromString(domain.getId()))
				.sourceAccountNumber(domain.getSourceAccountNumber())
				.targetAccountNumber(domain.getTargetAccountNumber()).amount(domain.getAmount()).type(domain.getType())
				.timestamp(domain.getTimestamp()).status(domain.getStatus()).failureReason(domain.getFailureReason())
				.isNew(isNewEntity).build();
	}

	private TransactionRecord toDomainModel(TransactionJpaEntity entity) {
		return TransactionRecord.reconstitute(entity.getId().toString(), entity.getSourceAccountNumber(),
				entity.getTargetAccountNumber(), entity.getAmount(), entity.getType(), entity.getTimestamp(),
				entity.getStatus(), entity.getFailureReason());
	}
}
