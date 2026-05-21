package com.example.demo.application.port.in;

import java.math.BigDecimal;

public interface TransferMoneyUseCase {

    void transfer(TransferCommand command);

    record TransferCommand(String sourceAccountNumber, String targetAccountNumber, BigDecimal amount) {
    }
}