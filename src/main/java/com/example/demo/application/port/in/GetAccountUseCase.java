package com.example.demo.application.port.in;

import com.example.demo.domain.model.Account;

public interface GetAccountUseCase {
    Account getAccount(String accountNumber, String requesterId);
}
