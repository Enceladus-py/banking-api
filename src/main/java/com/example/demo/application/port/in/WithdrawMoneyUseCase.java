package com.example.demo.application.port.in;

import com.example.demo.domain.model.Account;
import java.math.BigDecimal;

public interface WithdrawMoneyUseCase {

    Account withdraw(WithdrawCommand command);

    record WithdrawCommand(String accountId, BigDecimal amount, String requesterId) {
    }
}