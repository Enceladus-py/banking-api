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

        // Leverage Persistable<UUID> by constructing the entity directly and using the domain's isNew status,
        // avoiding the manual database SELECT (findById) before saving.
        AccountJpaEntity entity = new AccountJpaEntity();
        entity.setId(entityId);
        entity.setOwnerId(account.getOwnerId());
        entity.setAccountNumber(account.getAccountNumber());
        entity.setBalance(account.getBalance());
        entity.setNew(account.isNew());

        AccountJpaEntity savedEntity = repository.saveAndFlush(entity);

        return toDomainModel(savedEntity);
    }

    private Account toDomainModel(AccountJpaEntity entity) {
        return new Account(
                entity.getId().toString(),
                entity.getOwnerId(),
                entity.getAccountNumber(),
                entity.getBalance(),
                false); // Re-hydrated from DB, so isNew is false
    }

    @Override
    public Optional<Account> findByAccountNumber(String accountNumber) {
        return repository.findByAccountNumber(accountNumber).map(this::toDomainModel);
    }
}