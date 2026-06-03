package com.example.demo.transaction.application.service;

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

import com.example.demo.account.application.port.out.AccountRepository;
import com.example.demo.account.domain.model.Account;
import com.example.demo.common.domain.exception.EntityNotFoundException;
import com.example.demo.transaction.application.port.in.WithdrawMoneyUseCase.WithdrawCommand;
import com.example.demo.transaction.application.port.out.EventPublisher;
import com.example.demo.transaction.application.port.out.TransactionRecordRepository;
import com.example.demo.transaction.domain.event.TransactionPendingEvent;
import com.example.demo.transaction.domain.model.TransactionRecord;
import com.example.demo.transaction.domain.model.TransactionRecord.TransactionStatus;
import com.example.demo.transaction.domain.model.TransactionRecord.TransactionType;

@ExtendWith(MockitoExtension.class)
class WithdrawMoneyServiceTest {

	@Mock
	private AccountRepository accountRepository;

	@Mock
	private TransactionRecordRepository transactionRecordRepository;

	@Mock
	private EventPublisher eventPublisher;

	private WithdrawMoneyService withdrawMoneyService;

	@Captor
	private ArgumentCaptor<TransactionRecord> transactionCaptor;

	@Captor
	private ArgumentCaptor<TransactionPendingEvent> eventCaptor;

	@BeforeEach
	void setUp() {
		withdrawMoneyService = new WithdrawMoneyService(accountRepository, transactionRecordRepository, eventPublisher);
	}

	@Test
	void shouldSuccessfullyInitiateWithdrawal() {
		String requesterId = "USER-123";
		String accountNumber = "1234567890";
		Account existingAccount = new Account("uuid-1", requesterId, accountNumber, new BigDecimal("500.00"), 1L);
		WithdrawCommand command = new WithdrawCommand(accountNumber, new BigDecimal("150.00"), requesterId);

		when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(existingAccount));

		TransactionRecord result = withdrawMoneyService.withdraw(command);

		assertNotNull(result);
		assertEquals(TransactionStatus.PENDING, result.getStatus());

		verify(transactionRecordRepository, times(1)).save(transactionCaptor.capture());
		TransactionRecord pendingTx = transactionCaptor.getValue();
		assertEquals(accountNumber, pendingTx.getSourceAccountNumber());
		assertEquals(TransactionStatus.PENDING, pendingTx.getStatus());

		verify(eventPublisher, times(1)).publish(eventCaptor.capture());
		TransactionPendingEvent event = eventCaptor.getValue();
		assertEquals(pendingTx.getId(), event.transactionId());
		assertEquals(TransactionType.WITHDRAWAL, event.type());
	}

	@Test
	void shouldThrowSecurityExceptionWhenWithdrawingAsWrongUser() {
		String accountNumber = "1234567890";
		Account existingAccount = new Account("uuid-1", "REAL-OWNER", accountNumber, new BigDecimal("500.00"), 1L);
		WithdrawCommand command = new WithdrawCommand(accountNumber, new BigDecimal("150.00"), "HACKER");

		when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(existingAccount));

		assertThrows(SecurityException.class, () -> {
			withdrawMoneyService.withdraw(command);
		});

		verify(transactionRecordRepository, never()).save(any());
		verify(eventPublisher, never()).publish(any());
	}

	@Test
	void shouldThrowExceptionWhenAccountNotFoundForWithdrawal() {
		String accountNumber = "0000000000";
		WithdrawCommand command = new WithdrawCommand(accountNumber, new BigDecimal("100.00"), "USER-1");

		when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> {
			withdrawMoneyService.withdraw(command);
		});

		verify(transactionRecordRepository, never()).save(any());
		verify(eventPublisher, never()).publish(any());
	}

	@Test
	void shouldThrowIllegalArgumentExceptionWhenWithdrawalAmountIsZeroOrNegative() {
		String requesterId = "USER-123";
		String accountNumber = "1234567890";

		assertThrows(IllegalArgumentException.class, () -> {
			new WithdrawCommand(accountNumber, BigDecimal.ZERO, requesterId);
		});

		assertThrows(IllegalArgumentException.class, () -> {
			new WithdrawCommand(accountNumber, new BigDecimal("-50.00"), requesterId);
		});
	}

	@Test
	void shouldThrowIllegalArgumentExceptionWhenWithdrawCommandHasBlankFields() {
		String requesterId = "USER-123";
		String accountNumber = "1234567890";
		BigDecimal amount = new BigDecimal("100.00");

		assertThrows(IllegalArgumentException.class, () -> {
			new WithdrawCommand("  ", amount, requesterId);
		});

		assertThrows(IllegalArgumentException.class, () -> {
			new WithdrawCommand(accountNumber, amount, "  ");
		});
	}
}
