package com.example.demo.infrastructure.config;

import com.example.demo.application.port.out.AccountRepository;
import com.example.demo.application.port.out.TransactionRecordRepository;
import com.example.demo.application.port.out.UserRepository;
import com.example.demo.application.service.BankAccountService;
import com.example.demo.application.service.TransactionQueryService;
import com.example.demo.application.service.TransferService;
import com.example.demo.application.service.UserService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    /**
     * Spring will look for an implementation of AccountRepository.
     * Since we annotated PostgresAccountAdapter with @Component,
     * Spring finds it and automatically passes it into this method.
     */
    @Bean
    public BankAccountService bankAccountService(
            AccountRepository accountRepository,
            TransactionRecordRepository transactionRecordRepository,
            UserRepository userRepository) {
        // We manually construct our pure Java core and hand it to Spring
        return new BankAccountService(accountRepository, transactionRecordRepository, userRepository);
    }

    @Bean
    public TransferService transferService(
            AccountRepository accountRepository,
            TransactionRecordRepository transactionRecordRepository) {
        return new TransferService(accountRepository, transactionRecordRepository);
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