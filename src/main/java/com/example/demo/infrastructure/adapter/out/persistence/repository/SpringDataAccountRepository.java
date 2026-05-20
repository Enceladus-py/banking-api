package com.example.demo.infrastructure.adapter.out.persistence.repository;

import com.example.demo.infrastructure.adapter.out.persistence.entity.AccountJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SpringDataAccountRepository extends JpaRepository<AccountJpaEntity, UUID> {
    // We can add custom query methods here later if needed
}