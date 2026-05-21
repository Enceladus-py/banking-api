package com.example.demo.infrastructure.adapter.out.persistence;

import com.example.demo.domain.model.Account;
import com.example.demo.infrastructure.adapter.out.persistence.entity.AccountJpaEntity;
import com.example.demo.infrastructure.adapter.out.persistence.repository.SpringDataAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

// @DataJpaTest automatically replaces your Postgres DataSource with the H2 in-memory DB
@DataJpaTest
class PostgresAccountAdapterTest {

    @Autowired
    private SpringDataAccountRepository springDataRepository;

    private PostgresAccountAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new PostgresAccountAdapter(springDataRepository);
    }

    @Test
    void shouldMapAndSaveAccountToDatabase() {
        // 1. Arrange: Create a pure domain object
        Account domainAccount = new Account("Berat", "Dalsuna", "1234567890");

        // 2. Act: Save it through the adapter
        Account savedAccount = adapter.save(domainAccount);

        // 3. Assert: Verify the Domain model returned looks correct
        assertNotNull(savedAccount.getId());
        assertEquals("Berat", savedAccount.getName());
        assertEquals("Dalsuna", savedAccount.getSurname());
        assertEquals(BigDecimal.ZERO, savedAccount.getBalance());

        // 4. Deep Assert: Verify it actually went into the database correctly
        Optional<AccountJpaEntity> dbEntity = springDataRepository
                .findById(java.util.UUID.fromString(savedAccount.getId()));
        assertTrue(dbEntity.isPresent());
        assertEquals("Berat", dbEntity.get().getName());
        assertEquals(savedAccount.getAccountNumber(), dbEntity.get().getAccountNumber());
    }
}