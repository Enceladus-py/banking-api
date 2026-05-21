package com.example.demo.infrastructure.config;

import com.example.demo.application.port.out.AccountRepository;
import com.example.demo.application.port.out.TransactionRecordRepository;
import com.example.demo.application.service.BankAccountService;
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
            TransactionRecordRepository transactionRecordRepository) {
        // We manually construct our pure Java core and hand it to Spring
        return new BankAccountService(accountRepository, transactionRecordRepository);
    }
}