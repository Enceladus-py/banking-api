package com.example.demo.application.port.in;

import com.example.demo.domain.model.Account;
import java.math.BigDecimal;

public interface DepositMoneyUseCase {

    Account deposit(DepositCommand command);

    record DepositCommand(String accountId, BigDecimal amount, String requesterId) {
    }
}