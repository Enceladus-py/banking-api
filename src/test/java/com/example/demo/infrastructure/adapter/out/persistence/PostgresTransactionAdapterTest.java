package com.example.demo.infrastructure.adapter.out.persistence;

import com.example.demo.domain.model.TransactionRecord;
import com.example.demo.domain.model.TransactionRecord.TransactionType;
import com.example.demo.infrastructure.adapter.out.persistence.entity.TransactionJpaEntity;
import com.example.demo.infrastructure.adapter.out.persistence.repository.SpringDataTransactionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PostgresTransactionAdapterTest {

    private PostgresTransactionAdapter adapter;

    @Autowired
    private SpringDataTransactionRepository springDataRepository;

    @BeforeEach
    void setUp() {
        adapter = new PostgresTransactionAdapter(springDataRepository);
    }

    @AfterEach
    void tearDown() {
        // Enforce a strict clean slate between test runs
        springDataRepository.deleteAllInBatch();
    }

    @Test
    void shouldPersistTransactionRecordSuccessfully() {
        // Arrange
        TransactionRecord record = new TransactionRecord(
                "SOURCE1234",
                "TARGET1234",
                new BigDecimal("100.00"),
                TransactionType.TRANSFER);

        // Act
        adapter.save(record);

        // Assert: Query the database directly to verify the translation
        List<TransactionJpaEntity> savedEntities = springDataRepository.findAll();
        assertEquals(1, savedEntities.size());

        TransactionJpaEntity entity = savedEntities.get(0);
        assertEquals(record.getId(), entity.getId().toString());
        assertEquals("SOURCE1234", entity.getSourceAccountNumber());
        assertEquals("TARGET1234", entity.getTargetAccountNumber());
        // Use compareTo for BigDecimal to avoid scale mismatch issues (e.g., 100.00 vs
        // 100.0)
        assertEquals(0, new BigDecimal("100.00").compareTo(entity.getAmount()));
        assertEquals(TransactionType.TRANSFER, entity.getType());
        assertNotNull(entity.getTimestamp());
    }

    @Test
    void shouldFindTransactionsByAccountNumberSortedByTimestampDesc() {
        // Arrange
        String myAccount = "MYACC12345";
        LocalDateTime now = LocalDateTime.now();

        // 1. Older deposit (Target = myAccount)
        TransactionJpaEntity tx1 = new TransactionJpaEntity(
                UUID.randomUUID(),
                null,
                myAccount,
                new BigDecimal("50.00"),
                TransactionType.DEPOSIT,
                now.minusDays(2) // 2 days ago
        );

        // 2. Newer withdrawal (Source = myAccount)
        TransactionJpaEntity tx2 = new TransactionJpaEntity(
                UUID.randomUUID(),
                myAccount,
                null,
                new BigDecimal("20.00"),
                TransactionType.WITHDRAWAL,
                now.minusDays(1) // 1 day ago
        );

        // 3. Unrelated transaction
        TransactionJpaEntity tx3 = new TransactionJpaEntity(
                UUID.randomUUID(),
                "OTHERACCT1",
                "OTHERACCT2",
                new BigDecimal("10.00"),
                TransactionType.TRANSFER,
                now);

        // Save raw entities directly to bypass domain timestamp logic for testing
        springDataRepository.saveAll(List.of(tx1, tx2, tx3));

        // Act
        List<TransactionRecord> results = adapter.findByAccountNumber(myAccount);

        // Assert
        assertEquals(2, results.size(), "Should only fetch transactions related to the target account");

        // First result should be the newest one (tx2)
        TransactionRecord newest = results.get(0);
        assertEquals(tx2.getId().toString(), newest.getId());
        assertEquals(TransactionType.WITHDRAWAL, newest.getType());
        assertEquals(myAccount, newest.getSourceAccountNumber());

        // Second result should be the older one (tx1)
        TransactionRecord oldest = results.get(1);
        assertEquals(tx1.getId().toString(), oldest.getId());
        assertEquals(TransactionType.DEPOSIT, oldest.getType());
        assertEquals(myAccount, oldest.getTargetAccountNumber());
    }
}