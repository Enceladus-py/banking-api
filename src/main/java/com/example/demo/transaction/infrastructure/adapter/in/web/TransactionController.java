package com.example.demo.transaction.infrastructure.adapter.in.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.common.application.port.in.dto.PageRequest;
import com.example.demo.common.application.port.in.dto.PageResult;
import com.example.demo.transaction.application.port.in.DepositMoneyUseCase;
import com.example.demo.transaction.application.port.in.GetAccountTransactionsUseCase;
import com.example.demo.transaction.application.port.in.GetTransactionUseCase;
import com.example.demo.transaction.application.port.in.WithdrawMoneyUseCase;
import com.example.demo.transaction.domain.model.TransactionRecord;
import com.example.demo.transaction.infrastructure.adapter.in.web.dto.TransactionRequest;
import com.example.demo.transaction.infrastructure.adapter.in.web.dto.TransactionResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

	private final GetAccountTransactionsUseCase getAccountTransactionsUseCase;
	private final GetTransactionUseCase getTransactionUseCase;
	private final DepositMoneyUseCase depositMoneyUseCase;
	private final WithdrawMoneyUseCase withdrawMoneyUseCase;

	@PostMapping("/deposit")
	public ResponseEntity<TransactionResponse> depositMoney(@Valid @RequestBody TransactionRequest request,
			@RequestHeader("X-User-Id") String requesterId) {

		var command = new DepositMoneyUseCase.DepositCommand(request.accountNumber(), request.amount(), requesterId);
		var tx = depositMoneyUseCase.deposit(command);
		return ResponseEntity.accepted().body(TransactionResponse.from(tx));
	}

	@PostMapping("/withdraw")
	public ResponseEntity<TransactionResponse> withdrawMoney(@Valid @RequestBody TransactionRequest request,
			@RequestHeader("X-User-Id") String requesterId) {

		var command = new WithdrawMoneyUseCase.WithdrawCommand(request.accountNumber(), request.amount(), requesterId);
		var tx = withdrawMoneyUseCase.withdraw(command);
		return ResponseEntity.accepted().body(TransactionResponse.from(tx));
	}

	@GetMapping("/{accountNumber}")
	public ResponseEntity<PageResult<TransactionRecord>> getTransactions(@PathVariable String accountNumber,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestHeader("X-User-Id") String requesterId) {

		PageRequest pageRequest = new PageRequest(page, size);
		PageResult<TransactionRecord> result = getAccountTransactionsUseCase.getTransactions(accountNumber, pageRequest,
				requesterId);

		return ResponseEntity.ok(result);
	}

	@GetMapping("/id/{transactionId}")
	public ResponseEntity<TransactionResponse> getTransactionById(@PathVariable String transactionId,
			@RequestHeader("X-User-Id") String requesterId) {

		TransactionRecord record = getTransactionUseCase.getTransaction(transactionId, requesterId);
		return ResponseEntity.ok(TransactionResponse.from(record));
	}
}
