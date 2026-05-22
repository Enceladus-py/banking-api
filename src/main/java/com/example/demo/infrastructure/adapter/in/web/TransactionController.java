package com.example.demo.infrastructure.adapter.in.web;

import com.example.demo.application.port.in.GetAccountTransactionsUseCase;
import com.example.demo.application.port.in.dto.PageRequest;
import com.example.demo.application.port.in.dto.PageResult;
import com.example.demo.domain.model.TransactionRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final GetAccountTransactionsUseCase getAccountTransactionsUseCase;

    @GetMapping("/{accountNumber}")
    public ResponseEntity<PageResult<TransactionRecord>> getTransactions(
            @PathVariable String accountNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size, @RequestHeader("X-User-Id") String requesterId) {

        PageRequest pageRequest = new PageRequest(page, size);
        PageResult<TransactionRecord> result = getAccountTransactionsUseCase.getTransactions(accountNumber,
                pageRequest, requesterId);

        return ResponseEntity.ok(result);
    }
}