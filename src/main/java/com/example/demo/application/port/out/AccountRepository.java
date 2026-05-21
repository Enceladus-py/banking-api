package com.example.demo.application.port.out;

import com.example.demo.domain.model.Account;

public interface AccountRepository {
    Account save(Account account);

    boolean existsByAccountNumber(String accountNumber);
}