package com.example.demo.account.application.port.in;

import com.example.demo.account.domain.model.Account;

public interface GetAccountUseCase {
	Account getAccount(String accountNumber, String requesterId);
}
