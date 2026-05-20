package com.example.demo.infrastructure.adapter.out.persistence;

import com.example.demo.application.port.out.AccountRepository;
import com.example.demo.domain.model.Account;
import com.example.demo.infrastructure.adapter.out.persistence.entity.AccountJpaEntity;
import com.example.demo.infrastructure.adapter.out.persistence.repository.SpringDataAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostgresAccountAdapter implements AccountRepository {

    private final SpringDataAccountRepository repository;

    @Override
    public Account save(Account account) {
        // 1. Map Domain -> JPA Entity
        AccountJpaEntity entity = toJpaEntity(account);

        // 2. Save via Spring Data
        AccountJpaEntity savedEntity = repository.save(entity);

        // 3. Map JPA Entity -> Domain and return
        return toDomainModel(savedEntity);
    }

    // --- Private Mapping Methods ---

    private AccountJpaEntity toJpaEntity(Account account) {
        return AccountJpaEntity.builder()
                // If it's a new account, the ID will be null, which is fine (Hibernate
                // generates it)
                .id(account.getId() != null ? java.util.UUID.fromString(account.getId()) : null)
                .name(account.getName())
                .surname(account.getSurname())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .build();
    }

    private Account toDomainModel(AccountJpaEntity entity) {
        return new Account(
                entity.getId().toString(),
                entity.getName(),
                entity.getSurname(),
                entity.getAccountNumber(),
                entity.getBalance());
    }
}