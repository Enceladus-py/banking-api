package com.example.demo.application.port.in;

import com.example.demo.domain.model.Account;

public interface CreateAccountUseCase {

    Account createAccount(CreateAccountCommand command);

    // Immutable record carrying the exact data needed for this use case
    record CreateAccountCommand(String requesterId) {
    }
}