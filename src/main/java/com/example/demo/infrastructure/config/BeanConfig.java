package com.example.demo.infrastructure.config;

import com.example.demo.application.port.out.AccountRepository;
import com.example.demo.application.port.out.TransactionRecordRepository;
import com.example.demo.application.port.out.UserRepository;
import com.example.demo.application.port.out.EventPublisher;
import com.example.demo.application.service.CreateAccountService;
import com.example.demo.application.service.DepositMoneyService;
import com.example.demo.application.service.WithdrawMoneyService;
import com.example.demo.application.service.ProcessTransactionService;
import com.example.demo.application.service.TransactionQueryService;
import com.example.demo.application.service.TransferService;
import com.example.demo.application.service.UserService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public CreateAccountService createAccountService(
            AccountRepository accountRepository,
            UserRepository userRepository) {
        return new CreateAccountService(accountRepository, userRepository);
    }

    @Bean
    public DepositMoneyService depositMoneyService(
            AccountRepository accountRepository,
            TransactionRecordRepository transactionRecordRepository,
            EventPublisher eventPublisher) {
        return new DepositMoneyService(accountRepository, transactionRecordRepository, eventPublisher);
    }

    @Bean
    public WithdrawMoneyService withdrawMoneyService(
            AccountRepository accountRepository,
            TransactionRecordRepository transactionRecordRepository,
            EventPublisher eventPublisher) {
        return new WithdrawMoneyService(accountRepository, transactionRecordRepository, eventPublisher);
    }

    @Bean
    public ProcessTransactionService processTransactionService(
            AccountRepository accountRepository,
            TransactionRecordRepository transactionRecordRepository) {
        return new ProcessTransactionService(accountRepository, transactionRecordRepository);
    }

    @Bean
    public TransferService transferService(
            AccountRepository accountRepository,
            TransactionRecordRepository transactionRecordRepository,
            EventPublisher eventPublisher) {
        return new TransferService(accountRepository, transactionRecordRepository, eventPublisher);
    }

    @Bean
    public TransactionQueryService transactionQueryService(
            TransactionRecordRepository transactionRecordRepository, AccountRepository accountRepository) {
        return new TransactionQueryService(transactionRecordRepository, accountRepository);
    }

    @Bean
    public UserService userService(UserRepository userRepository) {
        return new UserService(userRepository);
    }
}