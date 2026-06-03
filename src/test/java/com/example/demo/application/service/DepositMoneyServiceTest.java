package com.example.demo.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.application.port.in.DepositMoneyUseCase.DepositCommand;
import com.example.demo.application.port.out.AccountRepository;
import com.example.demo.application.port.out.EventPublisher;
import com.example.demo.application.port.out.TransactionRecordRepository;
import com.example.demo.domain.event.TransactionPendingEvent;
import com.example.demo.domain.exception.EntityNotFoundException;
import com.example.demo.domain.model.Account;
import com.example.demo.domain.model.TransactionRecord;
import com.example.demo.domain.model.TransactionRecord.TransactionStatus;
import com.example.demo.domain.model.TransactionRecord.TransactionType;

@ExtendWith(MockitoExtension.class)
class DepositMoneyServiceTest {

	@Mock
	private AccountRepository accountRepository;

	@Mock
	private TransactionRecordRepository transactionRecordRepository;

	@Mock
	private EventPublisher eventPublisher;

	private DepositMoneyService depositMoneyService;

	@Captor
	private ArgumentCaptor<TransactionRecord> transactionCaptor;

	@Captor
	private ArgumentCaptor<TransactionPendingEvent> eventCaptor;

	@BeforeEach
	void setUp() {
		depositMoneyService = new DepositMoneyService(accountRepository, transactionRecordRepository, eventPublisher);
	}

	@Test
	void shouldSuccessfullyInitiateDeposit() {
		String requesterId = "USER-123";
		String accountNumber = "A1B2C3D4E5";
		DepositCommand command = new DepositCommand(accountNumber, new BigDecimal("250.00"), requesterId);
		Account existingAccount = new Account("uuid-123", requesterId, accountNumber, BigDecimal.ZERO, 1L);

		when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(existingAccount));

		TransactionRecord result = depositMoneyService.deposit(command);

		assertNotNull(result);
		assertEquals(TransactionStatus.PENDING, result.getStatus());

		verify(transactionRecordRepository, times(1)).save(transactionCaptor.capture());
		TransactionRecord pendingTx = transactionCaptor.getValue();
		assertEquals(accountNumber, pendingTx.getTargetAccountNumber());
		assertEquals(TransactionStatus.PENDING, pendingTx.getStatus());

		verify(eventPublisher, times(1)).publish(eventCaptor.capture());
		TransactionPendingEvent event = eventCaptor.getValue();
		assertEquals(pendingTx.getId(), event.transactionId());
		assertEquals(TransactionType.DEPOSIT, event.type());
	}

	@Test
	void shouldThrowSecurityExceptionWhenDepositingAsWrongUser() {
		String accountNumber = "A1B2C3D4E5";
		Account existingAccount = new Account("uuid-123", "REAL-OWNER", accountNumber, BigDecimal.ZERO, 1L);
		DepositCommand command = new DepositCommand(accountNumber, new BigDecimal("100.00"), "HACKER");

		when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(existingAccount));

		assertThrows(SecurityException.class, () -> {
			depositMoneyService.deposit(command);
		});

		verify(transactionRecordRepository, never()).save(any());
		verify(eventPublisher, never()).publish(any());
	}

	@Test
	void shouldThrowExceptionWhenAccountNotFoundForDeposit() {
		String nonExistentAccountNumber = "NOTFOUND12";
		DepositCommand command = new DepositCommand(nonExistentAccountNumber, new BigDecimal("100.00"), "USER-1");

		when(accountRepository.findByAccountNumber(nonExistentAccountNumber)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> {
			depositMoneyService.deposit(command);
		});

		verify(transactionRecordRepository, never()).save(any());
		verify(eventPublisher, never()).publish(any());
	}

	@Test
	void shouldThrowIllegalArgumentExceptionWhenDepositAmountIsZeroOrNegative() {
		String requesterId = "USER-123";
		String accountNumber = "A1B2C3D4E5";

		assertThrows(IllegalArgumentException.class, () -> {
			new DepositCommand(accountNumber, BigDecimal.ZERO, requesterId);
		});

		assertThrows(IllegalArgumentException.class, () -> {
			new DepositCommand(accountNumber, new BigDecimal("-50.00"), requesterId);
		});
	}

	@Test
	void shouldThrowIllegalArgumentExceptionWhenDepositCommandHasBlankFields() {
		String requesterId = "USER-123";
		String accountNumber = "A1B2C3D4E5";
		BigDecimal amount = new BigDecimal("100.00");

		assertThrows(IllegalArgumentException.class, () -> {
			new DepositCommand("  ", amount, requesterId);
		});

		assertThrows(IllegalArgumentException.class, () -> {
			new DepositCommand(accountNumber, amount, "  ");
		});
	}
}
