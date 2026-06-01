package com.example.demo.application.port.out;

import java.util.Optional;

import com.example.demo.domain.model.Account;

public interface AccountRepository {
    Account save(Account account);

    Optional<Account> findByAccountNumber(String accountId);

    Optional<Account> findByAccountNumberForWrite(String accountNumber);
}