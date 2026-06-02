package com.example.demo.infrastructure.adapter.in.web;

import com.example.demo.application.port.in.TransferMoneyUseCase;
import com.example.demo.infrastructure.adapter.in.web.dto.TransferRequest;
import com.example.demo.infrastructure.adapter.in.web.dto.TransactionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferMoneyUseCase transferMoneyUseCase;

    @PostMapping
    public ResponseEntity<TransactionResponse> transferMoney(@Valid @RequestBody TransferRequest request,
            @RequestHeader("X-User-Id") String requesterId) {

        TransferMoneyUseCase.TransferCommand command = new TransferMoneyUseCase.TransferCommand(
                request.sourceAccountNumber(),
                request.targetAccountNumber(),
                request.amount(),
                requesterId);

        var tx = transferMoneyUseCase.transfer(command);

        return ResponseEntity.accepted().body(TransactionResponse.from(tx));
    }
}