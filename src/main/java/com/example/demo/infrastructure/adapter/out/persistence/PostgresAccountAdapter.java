package com.example.demo.infrastructure.adapter.out.persistence;

import com.example.demo.application.port.out.AccountRepository;
import com.example.demo.domain.model.Account;
import com.example.demo.infrastructure.adapter.out.persistence.entity.AccountJpaEntity;
import com.example.demo.infrastructure.adapter.out.persistence.repository.SpringDataAccountRepository;
import lombok.RequiredArgsConstructor;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostgresAccountAdapter implements AccountRepository {

    private final SpringDataAccountRepository repository;

    @Override
    public Account save(Account account) {
        UUID entityId = UUID.fromString(account.getId());

        AccountJpaEntity managedEntity = repository.findById(entityId)
                .orElseGet(AccountJpaEntity::new);

        managedEntity.setId(entityId);
        managedEntity.setOwnerId(account.getOwnerId());
        managedEntity.setAccountNumber(account.getAccountNumber());
        managedEntity.setBalance(account.getBalance());

        AccountJpaEntity savedEntity = repository.saveAndFlush(managedEntity);

        return toDomainModel(savedEntity);
    }

    private Account toDomainModel(AccountJpaEntity entity) {
        return new Account(
                entity.getId().toString(),
                entity.getOwnerId(),
                entity.getAccountNumber(),
                entity.getBalance());
    }

    @Override
    public Optional<Account> findByAccountNumber(String accountNumber) {
        return repository.findByAccountNumber(accountNumber).map(this::toDomainModel);
    }
}