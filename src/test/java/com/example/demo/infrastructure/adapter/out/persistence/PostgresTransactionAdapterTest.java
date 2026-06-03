package com.example.demo.infrastructure.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.example.demo.application.port.in.dto.PageRequest;
import com.example.demo.application.port.in.dto.PageResult;
import com.example.demo.domain.model.TransactionRecord;
import com.example.demo.domain.model.TransactionRecord.TransactionStatus;
import com.example.demo.domain.model.TransactionRecord.TransactionType;
import com.example.demo.infrastructure.adapter.out.persistence.entity.TransactionJpaEntity;
import com.example.demo.infrastructure.adapter.out.persistence.repository.SpringDataTransactionRepository;

@DataJpaTest
class PostgresTransactionAdapterTest {

	private PostgresTransactionAdapter adapter;

	private final String targetAccount = "TARGET_ACCT";

	@Autowired
	private SpringDataTransactionRepository springDataRepository;

	@BeforeEach
	void setUp() {
		adapter = new PostgresTransactionAdapter(springDataRepository);

		LocalDateTime baseTime = LocalDateTime.now().minusDays(1);

		List<TransactionJpaEntity> entities = IntStream.range(0, 5)
				.mapToObj(i -> TransactionJpaEntity.builder().id(UUID.randomUUID()).sourceAccountNumber(null)
						.targetAccountNumber(targetAccount).amount(new BigDecimal("10.00"))
						.type(TransactionType.DEPOSIT).timestamp(baseTime.plusMinutes(i))
						.status(TransactionStatus.PENDING).build())
				.toList();

		// FIX: Force Hibernate to write the INSERTS to the database immediately
		springDataRepository.saveAllAndFlush(entities);
	}

	@AfterEach
	void tearDown() {
		// Enforce a strict clean slate between test runs
		springDataRepository.deleteAllInBatch();
	}

	@Test
	void shouldPersistTransactionRecordSuccessfully() {
		// Arrange
		TransactionRecord record = new TransactionRecord("SOURCE1234", "TARGET1234", new BigDecimal("100.00"),
				TransactionType.TRANSFER);

		// Act
		adapter.save(record);

		TransactionJpaEntity entity = springDataRepository.findById(UUID.fromString(record.getId()))
				.orElseThrow(() -> new AssertionError("Record was not saved to the database"));

		// Assert: Query the database directly to verify the translation
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
		TransactionJpaEntity tx1 = TransactionJpaEntity.builder().id(UUID.randomUUID()).sourceAccountNumber(null)
				.targetAccountNumber(myAccount).amount(new BigDecimal("50.00")).type(TransactionType.DEPOSIT)
				.timestamp(now.minusDays(2)).status(TransactionStatus.COMPLETED).build();

		// 2. Newer withdrawal (Source = myAccount)
		TransactionJpaEntity tx2 = TransactionJpaEntity.builder().id(UUID.randomUUID()).sourceAccountNumber(myAccount)
				.targetAccountNumber(null).amount(new BigDecimal("20.00")).type(TransactionType.WITHDRAWAL)
				.timestamp(now.minusDays(1)).status(TransactionStatus.COMPLETED).build();

		// 3. Unrelated transaction
		TransactionJpaEntity tx3 = TransactionJpaEntity.builder().id(UUID.randomUUID())
				.sourceAccountNumber("OTHERACCT1").targetAccountNumber("OTHERACCT2").amount(new BigDecimal("10.00"))
				.type(TransactionType.TRANSFER).timestamp(now).status(TransactionStatus.COMPLETED).build();

		// Save raw entities directly to bypass domain timestamp logic for testing
		springDataRepository.saveAll(List.of(tx1, tx2, tx3));

		// Act - We now pass a PageRequest
		PageRequest pageRequest = new PageRequest(0, 10);

		PageResult<TransactionRecord> pageResult = adapter.findByAccountNumber(myAccount, pageRequest);

		// Extract the list from the paginated result
		List<TransactionRecord> results = pageResult.content();

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

	@Test
	void shouldReturnFirstPageOfTransactions() {
		// Arrange: Request page 0, size 2
		PageRequest pageRequest = new PageRequest(0, 2);

		// Act
		PageResult<TransactionRecord> result = adapter.findByAccountNumber(targetAccount, pageRequest);

		// Assert
		assertEquals(2, result.content().size());
		assertEquals(0, result.pageNumber());
		assertEquals(2, result.pageSize());
		assertEquals(5, result.totalElements());
		assertEquals(3, result.totalPages()); // 5 items divided by 2 per page = 3 pages
	}

	@Test
	void shouldReturnLastPageOfTransactionsWithRemainingItems() {
		// Arrange: Request page 2 (the 3rd page), size 2
		PageRequest pageRequest = new PageRequest(2, 2);

		// Act
		PageResult<TransactionRecord> result = adapter.findByAccountNumber(targetAccount, pageRequest);

		// Assert
		assertEquals(1, result.content().size(), "Last page should only have 1 item");
		assertEquals(2, result.pageNumber());
		assertEquals(2, result.pageSize());
		assertEquals(5, result.totalElements());
		assertEquals(3, result.totalPages());
	}
}
