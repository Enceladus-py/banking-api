package com.example.demo.application.port.in;

import com.example.demo.domain.model.TransactionRecord;
import java.math.BigDecimal;

public interface TransferMoneyUseCase {

    TransactionRecord transfer(TransferCommand command);

    record TransferCommand(String sourceAccountNumber, String targetAccountNumber, BigDecimal amount,
            String requesterId) {
        public TransferCommand {
            java.util.Objects.requireNonNull(sourceAccountNumber, "Source account number is required");
            if (sourceAccountNumber.trim().isBlank()) {
                throw new IllegalArgumentException("Source account number cannot be blank");
            }
            java.util.Objects.requireNonNull(targetAccountNumber, "Target account number is required");
            if (targetAccountNumber.trim().isBlank()) {
                throw new IllegalArgumentException("Target account number cannot be blank");
            }
            java.util.Objects.requireNonNull(requesterId, "Requester ID is required");
            if (requesterId.trim().isBlank()) {
                throw new IllegalArgumentException("Requester ID cannot be blank");
            }
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Transfer amount must be strictly positive");
            }
            if (sourceAccountNumber.equals(targetAccountNumber)) {
                throw new IllegalArgumentException("Cannot transfer to the same account");
            }
        }
    }
}