package com.example.demo.infrastructure.config;

import com.example.demo.application.port.out.AccountRepository;
import com.example.demo.application.port.out.TransactionRecordRepository;
import com.example.demo.application.port.out.UserRepository;
import com.example.demo.application.port.out.EventPublisher;
import com.example.demo.application.service.AccountQueryService;
import com.example.demo.application.service.CreateAccountService;
import com.example.demo.application.service.DepositMoneyService;
import com.example.demo.application.service.WithdrawMoneyService;
import com.example.demo.application.service.ProcessTransactionService;
import com.example.demo.application.service.TransactionQueryService;
import com.example.demo.application.service.TransferService;
import com.example.demo.application.service.UserService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;

import com.example.demo.application.port.in.CreateAccountUseCase;
import com.example.demo.application.port.in.DepositMoneyUseCase;
import com.example.demo.application.port.in.GetAccountTransactionsUseCase;
import com.example.demo.application.port.in.GetAccountUseCase;
import com.example.demo.application.port.in.GetTransactionUseCase;
import com.example.demo.application.port.in.GetUserUseCase;
import com.example.demo.application.port.in.ProcessTransactionUseCase;
import com.example.demo.application.port.in.RegisterUserUseCase;
import com.example.demo.application.port.in.TransferMoneyUseCase;
import com.example.demo.application.port.in.WithdrawMoneyUseCase;

@Configuration
public class BeanConfig {

    @Bean
    public CreateAccountUseCase createAccountUseCase(
            AccountRepository accountRepository,
            UserRepository userRepository,
            TransactionTemplate txTemplate) {
        CreateAccountService service = new CreateAccountService(accountRepository, userRepository);
        return command -> txTemplate.execute(status -> service.createAccount(command));
    }

    @Bean
    public DepositMoneyUseCase depositMoneyUseCase(
            AccountRepository accountRepository,
            TransactionRecordRepository transactionRecordRepository,
            EventPublisher eventPublisher,
            TransactionTemplate txTemplate) {
        DepositMoneyService service = new DepositMoneyService(accountRepository, transactionRecordRepository, eventPublisher);
        return command -> txTemplate.execute(status -> service.deposit(command));
    }

    @Bean
    public WithdrawMoneyUseCase withdrawMoneyUseCase(
            AccountRepository accountRepository,
            TransactionRecordRepository transactionRecordRepository,
            EventPublisher eventPublisher,
            TransactionTemplate txTemplate) {
        WithdrawMoneyService service = new WithdrawMoneyService(accountRepository, transactionRecordRepository, eventPublisher);
        return command -> txTemplate.execute(status -> service.withdraw(command));
    }

    @Bean
    public ProcessTransactionUseCase processTransactionUseCase(
            AccountRepository accountRepository,
            TransactionRecordRepository transactionRecordRepository,
            TransactionTemplate txTemplate) {
        ProcessTransactionService service = new ProcessTransactionService(accountRepository, transactionRecordRepository);
        return event -> txTemplate.executeWithoutResult(status -> service.process(event));
    }

    @Bean
    public TransferMoneyUseCase transferMoneyUseCase(
            AccountRepository accountRepository,
            TransactionRecordRepository transactionRecordRepository,
            EventPublisher eventPublisher,
            TransactionTemplate txTemplate) {
        TransferService service = new TransferService(accountRepository, transactionRecordRepository, eventPublisher);
        return command -> txTemplate.execute(status -> service.transfer(command));
    }

    @Bean
    public GetAccountTransactionsUseCase getAccountTransactionsUseCase(
            TransactionRecordRepository transactionRecordRepository, AccountRepository accountRepository) {
        // Reads often don't need explicit programmatic transactions in simple cases
        return new TransactionQueryService(transactionRecordRepository, accountRepository);
    }

    @Bean
    public GetTransactionUseCase getTransactionUseCase(
            TransactionRecordRepository transactionRecordRepository, AccountRepository accountRepository) {
        return new TransactionQueryService(transactionRecordRepository, accountRepository);
    }

    @Bean
    public GetAccountUseCase getAccountUseCase(AccountRepository accountRepository) {
        return new AccountQueryService(accountRepository);
    }

    @Bean
    public RegisterUserUseCase registerUserUseCase(UserRepository userRepository, TransactionTemplate txTemplate) {
        UserService service = new UserService(userRepository);
        return command -> txTemplate.execute(status -> service.registerUser(command));
    }

    @Bean
    public GetUserUseCase getUserUseCase(UserRepository userRepository) {
        // Reads don't require explicit transaction wrappers for simple queries
        UserService service = new UserService(userRepository);
        return service;
    }
}