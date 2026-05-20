package com.example.demo.infrastructure.config;

import com.example.demo.application.port.out.AccountRepository;
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
    public BankAccountService bankAccountService(AccountRepository accountRepository) {
        // We manually instantiate our pure Java service, injecting the adapter
        return new BankAccountService(accountRepository);
    }
}