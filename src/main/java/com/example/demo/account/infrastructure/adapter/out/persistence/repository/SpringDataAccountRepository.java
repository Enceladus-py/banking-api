package com.example.demo.account.infrastructure.adapter.out.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.account.infrastructure.adapter.out.persistence.entity.AccountJpaEntity;

import jakarta.persistence.LockModeType;

public interface SpringDataAccountRepository extends JpaRepository<AccountJpaEntity, UUID> {
	Optional<AccountJpaEntity> findByAccountNumber(String accountNumber);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT a FROM AccountJpaEntity a WHERE a.accountNumber = :accountNumber")
	Optional<AccountJpaEntity> findByAccountNumberForWrite(@Param("accountNumber") String accountNumber);
}
