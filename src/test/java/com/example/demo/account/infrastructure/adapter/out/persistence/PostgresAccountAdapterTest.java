package com.example.demo.account.infrastructure.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.example.demo.account.domain.model.Account;
import com.example.demo.account.infrastructure.adapter.out.persistence.entity.AccountJpaEntity;
import com.example.demo.account.infrastructure.adapter.out.persistence.repository.SpringDataAccountRepository;

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
		String ownerId = "USER-12345";
		Account domainAccount = Account.createNew(ownerId, "1234567890");

		// 2. Act: Save it through the adapter
		Account savedAccount = adapter.save(domainAccount);

		// 3. Assert: Verify the Domain model returned looks correct
		assertNotNull(savedAccount.getId());
		assertEquals(ownerId, savedAccount.getOwnerId());
		assertEquals("1234567890", savedAccount.getAccountNumber());
		assertEquals(BigDecimal.ZERO, savedAccount.getBalance());

		// 4. Deep Assert: Verify it actually went into the database correctly
		Optional<AccountJpaEntity> dbEntity = springDataRepository.findById(UUID.fromString(savedAccount.getId()));

		assertTrue(dbEntity.isPresent());
		assertEquals(ownerId, dbEntity.get().getOwnerId());
		assertEquals(savedAccount.getAccountNumber(), dbEntity.get().getAccountNumber());
		assertEquals(0, BigDecimal.ZERO.compareTo(dbEntity.get().getBalance()));
	}

	@Test
	void shouldUpdateExistingAccountBalanceSuccessfullyWithoutStateCollisions() {
		// Arrange: Directly populate an account row into the database first
		String ownerId = "USER-98765";
		Account baseAccount = Account.createNew(ownerId, "9876543210");
		adapter.save(baseAccount);

		// Act: Retrieve it, apply a deposit mutation, and save it back
		Optional<Account> fetchedOpt = adapter.findByAccountNumber("9876543210");
		assertTrue(fetchedOpt.isPresent());

		Account domainModel = fetchedOpt.get();
		domainModel.deposit(new BigDecimal("250.50"));

		Account updatedAccount = adapter.save(domainModel);

		// Assert: Ensure identity remains stable while balance scales
		assertEquals(baseAccount.getId(), updatedAccount.getId());
		assertEquals(0, new BigDecimal("250.50").compareTo(updatedAccount.getBalance()));
	}

	@Test
	void shouldFindByAccountNumberForWriteSuccessfully() {
		// Arrange
		String ownerId = "USER-11111";
		String accountNumber = "1111111111";
		Account baseAccount = Account.createNew(ownerId, accountNumber);
		adapter.save(baseAccount);

		// Act
		Optional<Account> fetchedOpt = adapter.lockAndLoad(accountNumber);

		// Assert
		assertTrue(fetchedOpt.isPresent());
		assertEquals(baseAccount.getId(), fetchedOpt.get().getId());
		assertEquals(ownerId, fetchedOpt.get().getOwnerId());
		assertEquals(0, BigDecimal.ZERO.compareTo(fetchedOpt.get().getBalance()));
	}
	@Test
	void shouldFindByAccountNumberSuccessfully() {
		// Arrange
		String ownerId = "USER-22222";
		String accountNumber = "2222222222";
		Account baseAccount = Account.createNew(ownerId, accountNumber);
		adapter.save(baseAccount);

		// Act
		Optional<Account> fetchedOpt = adapter.findByAccountNumber(accountNumber);

		// Assert
		assertTrue(fetchedOpt.isPresent());
		assertEquals(baseAccount.getId(), fetchedOpt.get().getId());
		assertEquals(ownerId, fetchedOpt.get().getOwnerId());
		assertEquals(0, BigDecimal.ZERO.compareTo(fetchedOpt.get().getBalance()));
	}

	@Test
	void shouldFindByOwnerIdSuccessfully() {
		// Arrange
		String ownerId = "USER-33333";
		String accountNumber = "3333333333";
		Account baseAccount = Account.createNew(ownerId, accountNumber);
		adapter.save(baseAccount);

		// Act
		com.example.demo.common.application.port.in.dto.PageRequest pageRequest = new com.example.demo.common.application.port.in.dto.PageRequest(
				0, 10);
		com.example.demo.common.application.port.in.dto.PageResult<Account> result = adapter.findByOwnerId(ownerId,
				pageRequest);

		// Assert
		assertNotNull(result);
		assertEquals(1, result.totalElements());
		assertEquals(baseAccount.getId(), result.content().get(0).getId());
	}
}
