package com.example.demo.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record DepositRequest(
        @NotNull(message = "Account number is required") @Size(min = 10, max = 10, message = "Account number must be exactly 10 characters") String accountNumber,

        @NotNull(message = "Deposit amount is required") @Positive(message = "Deposit amount must be greater than zero") BigDecimal amount) {
}