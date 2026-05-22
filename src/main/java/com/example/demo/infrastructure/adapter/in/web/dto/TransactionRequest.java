package com.example.demo.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransactionRequest(
        @NotBlank(message = "Account number is required") String accountNumber,

        @NotNull(message = "Amount is required") @Positive(message = "Amount must be greater than zero") BigDecimal amount) {
}