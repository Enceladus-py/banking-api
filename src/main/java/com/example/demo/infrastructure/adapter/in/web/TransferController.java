package com.example.demo.infrastructure.adapter.in.web;

import com.example.demo.application.port.in.TransferMoneyUseCase;
import com.example.demo.infrastructure.adapter.in.web.dto.TransferRequest;
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
    public ResponseEntity<Void> transferMoney(@Valid @RequestBody TransferRequest request,
            @RequestHeader("X-User-Id") String requesterId) {

        TransferMoneyUseCase.TransferCommand command = new TransferMoneyUseCase.TransferCommand(
                request.sourceAccountNumber(),
                request.targetAccountNumber(),
                request.amount(),
                requesterId);

        transferMoneyUseCase.transfer(command);

        // Returning 200 OK signals a successful transaction.
        return ResponseEntity.ok().build();
    }
}