package com.example.demo.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.example.demo.account.application.port.in.AccountOperationsPort;
import com.example.demo.account.domain.model.Account;
import com.example.demo.common.domain.exception.EntityNotFoundException;
import com.example.demo.transaction.application.port.in.DepositMoneyUseCase;
import com.example.demo.transaction.application.port.in.DepositMoneyUseCase.DepositCommand;
import com.example.demo.transaction.application.port.in.TransferMoneyUseCase;
import com.example.demo.transaction.application.port.in.TransferMoneyUseCase.TransferCommand;
import com.example.demo.transaction.application.port.in.WithdrawMoneyUseCase;
import com.example.demo.transaction.application.port.in.WithdrawMoneyUseCase.WithdrawCommand;
import com.example.demo.transaction.domain.model.TransactionRecord;
import com.example.demo.transaction.domain.model.TransactionRecord.TransactionStatus;

/**
 * Spring Modulith integration test for the {@code transaction} module.
 *
 * <p>
 * Bootstraps only the beans belonging to the {@code transaction} module. The
 * cross-module {@link AccountOperationsPort} dependency (owned by the
 * {@code account} module) is satisfied via a {@link MockitoBean} so the test
 * stays isolated from the account module's persistence layer.
 */
@ApplicationModuleTest
@org.springframework.context.annotation.ComponentScan(basePackageClasses = TransactionModuleTest.class, includeFilters = @org.springframework.context.annotation.ComponentScan.Filter(type = org.springframework.context.annotation.FilterType.ANNOTATION, classes = com.example.demo.common.application.annotation.UseCase.class))
class TransactionModuleTest {

	@Autowired
	DepositMoneyUseCase depositMoneyUseCase;

	@Autowired
	WithdrawMoneyUseCase withdrawMoneyUseCase;

	@Autowired
	TransferMoneyUseCase transferMoneyUseCase;

	@MockitoBean
	AccountOperationsPort accountOperationsPort;

	@MockitoBean
	KafkaTemplate<String, String> kafkaTemplate;

	private static final String OWNER = "user-tx-test";
	private static final String ACC_A = "ACCA000001";
	private static final String ACC_B = "ACCB000002";

	@Test
	void shouldInitiateDepositAndReturnPendingRecord() {
		Account account = Account.reconstitute("id-a", OWNER, ACC_A, BigDecimal.ZERO, 1L);
		when(accountOperationsPort.findByAccountNumber(ACC_A)).thenReturn(Optional.of(account));

		TransactionRecord result = depositMoneyUseCase
				.deposit(new DepositCommand(ACC_A, new BigDecimal("100.00"), OWNER));

		assertThat(result).isNotNull();
		assertThat(result.getStatus()).isEqualTo(TransactionStatus.PENDING);
		assertThat(result.getTargetAccountNumber()).isEqualTo(ACC_A);
	}

	@Test
	void shouldInitiateWithdrawalAndReturnPendingRecord() {
		Account account = Account.reconstitute("id-a", OWNER, ACC_A, new BigDecimal("500.00"), 1L);
		when(accountOperationsPort.findByAccountNumber(ACC_A)).thenReturn(Optional.of(account));

		TransactionRecord result = withdrawMoneyUseCase
				.withdraw(new WithdrawCommand(ACC_A, new BigDecimal("200.00"), OWNER));

		assertThat(result).isNotNull();
		assertThat(result.getStatus()).isEqualTo(TransactionStatus.PENDING);
		assertThat(result.getSourceAccountNumber()).isEqualTo(ACC_A);
	}

	@Test
	void shouldInitiateTransferAndReturnPendingRecord() {
		Account source = Account.reconstitute("id-a", OWNER, ACC_A, new BigDecimal("300.00"), 1L);
		Account target = Account.reconstitute("id-b", "other-user", ACC_B, BigDecimal.ZERO, 1L);
		when(accountOperationsPort.findByAccountNumber(ACC_A)).thenReturn(Optional.of(source));
		when(accountOperationsPort.findByAccountNumber(ACC_B)).thenReturn(Optional.of(target));

		TransactionRecord result = transferMoneyUseCase
				.transfer(new TransferCommand(ACC_A, ACC_B, new BigDecimal("50.00"), OWNER));

		assertThat(result).isNotNull();
		assertThat(result.getStatus()).isEqualTo(TransactionStatus.PENDING);
		assertThat(result.getSourceAccountNumber()).isEqualTo(ACC_A);
		assertThat(result.getTargetAccountNumber()).isEqualTo(ACC_B);
	}

	@Test
	void shouldRejectDepositWhenAccountNotFound() {
		when(accountOperationsPort.findByAccountNumber(ACC_A)).thenReturn(Optional.empty());

		assertThatThrownBy(
				() -> depositMoneyUseCase.deposit(new DepositCommand(ACC_A, new BigDecimal("100.00"), OWNER)))
				.isInstanceOf(EntityNotFoundException.class);
	}

	@Test
	void shouldRejectTransferWhenCallerIsNotOwner() {
		Account source = Account.reconstitute("id-a", OWNER, ACC_A, new BigDecimal("300.00"), 1L);
		when(accountOperationsPort.findByAccountNumber(ACC_A)).thenReturn(Optional.of(source));

		assertThatThrownBy(() -> transferMoneyUseCase
				.transfer(new TransferCommand(ACC_A, ACC_B, new BigDecimal("50.00"), "attacker")))
				.isInstanceOf(SecurityException.class);
	}
}
