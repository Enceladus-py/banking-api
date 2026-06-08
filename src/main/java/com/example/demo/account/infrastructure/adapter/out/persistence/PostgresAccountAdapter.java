package com.example.demo.account.infrastructure.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.example.demo.account.application.port.out.AccountRepository;
import com.example.demo.account.domain.model.Account;
import com.example.demo.account.infrastructure.adapter.out.persistence.entity.AccountJpaEntity;
import com.example.demo.account.infrastructure.adapter.out.persistence.repository.SpringDataAccountRepository;
import com.example.demo.common.application.port.in.dto.PageRequest;
import com.example.demo.common.application.port.in.dto.PageResult;

/**
 * Persistence adapter implementing AccountRepository for PostgreSQL database.
 */
@Component
public class PostgresAccountAdapter implements AccountRepository {

	private final SpringDataAccountRepository repository;

	/**
	 * Constructs a new PostgresAccountAdapter with the specified Spring Data
	 * repository.
	 *
	 * @param repository
	 *            the Spring Data JPA repository
	 */
	public PostgresAccountAdapter(SpringDataAccountRepository repository) {
		this.repository = repository;
	}

	@Override
	public Account save(Account account) {
		UUID entityId = UUID.fromString(account.getId());

		// Construct entity directly and map version. Spring Data JPA uses the version
		// field (null = new)
		// to decide persist vs merge automatically, avoiding any pre-save SELECT.
		AccountJpaEntity entity = new AccountJpaEntity();
		entity.setId(entityId);
		entity.setOwnerId(account.getOwnerId());
		entity.setAccountNumber(account.getAccountNumber());
		entity.setBalance(account.getBalance());
		entity.setVersion(account.getVersion());

		AccountJpaEntity savedEntity = repository.saveAndFlush(entity);

		return toDomainModel(savedEntity);
	}

	private Account toDomainModel(AccountJpaEntity entity) {
		return Account.reconstitute(entity.getId().toString(), entity.getOwnerId(), entity.getAccountNumber(),
				entity.getBalance(), entity.getVersion()); // Re-hydrated with DB version
	}

	@Override
	public Optional<Account> findByAccountNumber(String accountNumber) {
		return repository.findByAccountNumber(accountNumber).map(this::toDomainModel);
	}

	@Override
	public Optional<Account> lockAndLoad(String accountNumber) {
		return repository.findByAccountNumberForWrite(accountNumber).map(this::toDomainModel);
	}

	@Override
	public PageResult<Account> findByOwnerId(String ownerId, PageRequest pageRequest) {
		Pageable pageable = org.springframework.data.domain.PageRequest.of(pageRequest.pageNumber(),
				pageRequest.pageSize());
		Page<AccountJpaEntity> entityPage = repository.findByOwnerId(ownerId, pageable);

		return new PageResult<>(entityPage.getContent().stream().map(this::toDomainModel).toList(),
				entityPage.getNumber(), entityPage.getSize(), entityPage.getTotalElements(),
				entityPage.getTotalPages());
	}
}
