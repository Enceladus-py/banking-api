package com.fintech.banking.coreapi.transaction.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import com.fintech.banking.coreapi.account.infrastructure.adapter.out.persistence.entity.AccountJpaEntity;
import com.fintech.banking.coreapi.account.infrastructure.adapter.out.persistence.repository.SpringDataAccountRepository;
import com.fintech.banking.coreapi.transaction.application.port.in.DepositMoneyUseCase;
import com.fintech.banking.coreapi.transaction.application.port.in.TransferMoneyUseCase;
import com.fintech.banking.coreapi.transaction.application.port.in.WithdrawMoneyUseCase;
import com.fintech.banking.coreapi.transaction.application.port.out.EventPublisher;
import com.fintech.banking.coreapi.transaction.infrastructure.adapter.out.persistence.repository.SpringDataTransactionRepository;
import com.fintech.banking.coreapi.user.infrastructure.adapter.out.persistence.entity.UserJpaEntity;
import com.fintech.banking.coreapi.user.infrastructure.adapter.out.persistence.repository.SpringDataUserRepository;

@SpringBootTest(properties = {"outbox.scheduler.delay=9999999"})
@org.springframework.kafka.test.context.EmbeddedKafka(partitions = 1)
@org.springframework.test.context.TestPropertySource(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
class TransactionRollbackIntegrationTest {

	@Autowired
	private DepositMoneyUseCase depositMoneyUseCase;

	@Autowired
	private WithdrawMoneyUseCase withdrawMoneyUseCase;

	@Autowired
	private TransferMoneyUseCase transferMoneyUseCase;

	@MockitoSpyBean
	private EventPublisher eventPublisher;

	@Autowired
	private SpringDataUserRepository userRepository;

	@Autowired
	private SpringDataAccountRepository accountRepository;

	@Autowired
	private SpringDataTransactionRepository transactionRepository;

	private final String userId = UUID.randomUUID().toString();
	private final String accountNumber = "ROLLBACK123";

	@BeforeEach
	void setUp() {
		transactionRepository.deleteAllInBatch();
		accountRepository.deleteAllInBatch();
		userRepository.deleteAllInBatch();

		var userEntity = new UserJpaEntity(UUID.fromString(userId), "Rollback", "Tester", null);
		userRepository.save(userEntity);

		var accountEntity = new AccountJpaEntity(UUID.randomUUID(), accountNumber, BigDecimal.ZERO, userId, false);
		accountRepository.save(accountEntity);
	}

	@AfterEach
	void tearDown() {
		transactionRepository.deleteAllInBatch();
		accountRepository.deleteAllInBatch();
		userRepository.deleteAllInBatch();
	}

	@Test
	void shouldRollbackTransactionRecordWhenEventPublisherThrowsException() {
		// Arrange
		DepositMoneyUseCase.DepositCommand command = new DepositMoneyUseCase.DepositCommand(accountNumber,
				new BigDecimal("100.00"), userId);

		// Force the EventPublisher (outbox save) to fail, which is called AFTER the
		// transaction record save
		doThrow(new RuntimeException("Simulated database failure during outbox insert")).when(eventPublisher)
				.publish(any());

		long transactionCountBefore = transactionRepository.count();

		// Act & Assert
		assertThrows(RuntimeException.class, () -> {
			depositMoneyUseCase.deposit(command);
		});

		// Verify that the transaction record was NOT saved (it was rolled back)
		long transactionCountAfter = transactionRepository.count();
		assertEquals(transactionCountBefore, transactionCountAfter,
				"Transaction record should not be saved because the transaction rolled back");
	}

	@Test
	void shouldRollbackTransactionRecordWhenEventPublisherThrowsExceptionOnWithdraw() {
		// Arrange
		var accountEntity = accountRepository.findByAccountNumber(accountNumber).get();
		accountEntity.setBalance(new BigDecimal("100.00"));
		accountRepository.save(accountEntity);

		WithdrawMoneyUseCase.WithdrawCommand command = new WithdrawMoneyUseCase.WithdrawCommand(accountNumber,
				new BigDecimal("50.00"), userId);

		doThrow(new RuntimeException("Simulated database failure during outbox insert")).when(eventPublisher)
				.publish(any());

		long transactionCountBefore = transactionRepository.count();

		// Act & Assert
		assertThrows(RuntimeException.class, () -> {
			withdrawMoneyUseCase.withdraw(command);
		});

		// Verify rollback
		long transactionCountAfter = transactionRepository.count();
		assertEquals(transactionCountBefore, transactionCountAfter,
				"Transaction record should not be saved because the transaction rolled back");
	}

	@Test
	void shouldRollbackTransactionRecordWhenEventPublisherThrowsExceptionOnTransfer() {
		// Arrange
		// Target account needs to exist
		String targetAccountNumber = "TARGET123";
		var targetAccountEntity = new AccountJpaEntity(UUID.randomUUID(), targetAccountNumber, BigDecimal.ZERO, userId,
				false);
		accountRepository.save(targetAccountEntity);

		TransferMoneyUseCase.TransferCommand command = new TransferMoneyUseCase.TransferCommand(accountNumber,
				targetAccountNumber, new BigDecimal("25.00"), userId);

		doThrow(new RuntimeException("Simulated database failure during outbox insert")).when(eventPublisher)
				.publish(any());

		long transactionCountBefore = transactionRepository.count();

		// Act & Assert
		assertThrows(RuntimeException.class, () -> {
			transferMoneyUseCase.transfer(command);
		});

		// Verify rollback
		long transactionCountAfter = transactionRepository.count();
		assertEquals(transactionCountBefore, transactionCountAfter,
				"Transaction record should not be saved because the transaction rolled back");
	}
}
