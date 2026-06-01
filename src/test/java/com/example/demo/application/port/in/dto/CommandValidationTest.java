package com.example.demo.application.port.in.dto;

import com.example.demo.application.port.in.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CommandValidationTest {

    @Test
    void shouldCreateValidTransferCommand() {
        TransferMoneyUseCase.TransferCommand command = new TransferMoneyUseCase.TransferCommand(
                "SRC1234567", "TGT1234567", new BigDecimal("100.00"), "USER-1"
        );
        assertEquals("SRC1234567", command.sourceAccountNumber());
        assertEquals("TGT1234567", command.targetAccountNumber());
        assertEquals(new BigDecimal("100.00"), command.amount());
        assertEquals("USER-1", command.requesterId());
    }

    @Test
    void shouldThrowExceptionWhenTransferCommandHasInvalidParameters() {
        // Blank account numbers
        assertThrows(IllegalArgumentException.class, () -> 
            new TransferMoneyUseCase.TransferCommand("  ", "TGT1234567", new BigDecimal("100.00"), "USER-1")
        );
        assertThrows(IllegalArgumentException.class, () -> 
            new TransferMoneyUseCase.TransferCommand("SRC1234567", "", new BigDecimal("100.00"), "USER-1")
        );
        // Null or blank requester
        assertThrows(IllegalArgumentException.class, () -> 
            new TransferMoneyUseCase.TransferCommand("SRC1234567", "TGT1234567", new BigDecimal("100.00"), "  ")
        );
        // Negative or zero amount
        assertThrows(IllegalArgumentException.class, () -> 
            new TransferMoneyUseCase.TransferCommand("SRC1234567", "TGT1234567", new BigDecimal("-50.00"), "USER-1")
        );
        assertThrows(IllegalArgumentException.class, () -> 
            new TransferMoneyUseCase.TransferCommand("SRC1234567", "TGT1234567", BigDecimal.ZERO, "USER-1")
        );
        // Transfer to same account
        assertThrows(IllegalArgumentException.class, () -> 
            new TransferMoneyUseCase.TransferCommand("SRC1234567", "SRC1234567", new BigDecimal("10.00"), "USER-1")
        );
    }

    @Test
    void shouldCreateValidDepositCommand() {
        DepositMoneyUseCase.DepositCommand command = new DepositMoneyUseCase.DepositCommand(
                "ACC1234567", new BigDecimal("50.00"), "USER-1"
        );
        assertEquals("ACC1234567", command.accountId());
        assertEquals(new BigDecimal("50.00"), command.amount());
        assertEquals("USER-1", command.requesterId());
    }

    @Test
    void shouldThrowExceptionWhenDepositCommandHasInvalidParameters() {
        assertThrows(IllegalArgumentException.class, () -> 
            new DepositMoneyUseCase.DepositCommand("  ", new BigDecimal("50.00"), "USER-1")
        );
        assertThrows(IllegalArgumentException.class, () -> 
            new DepositMoneyUseCase.DepositCommand("ACC1234567", new BigDecimal("-10.00"), "USER-1")
        );
        assertThrows(IllegalArgumentException.class, () -> 
            new DepositMoneyUseCase.DepositCommand("ACC1234567", new BigDecimal("50.00"), "")
        );
    }

    @Test
    void shouldCreateValidWithdrawCommand() {
        WithdrawMoneyUseCase.WithdrawCommand command = new WithdrawMoneyUseCase.WithdrawCommand(
                "ACC1234567", new BigDecimal("50.00"), "USER-1"
        );
        assertEquals("ACC1234567", command.accountId());
        assertEquals(new BigDecimal("50.00"), command.amount());
        assertEquals("USER-1", command.requesterId());
    }

    @Test
    void shouldThrowExceptionWhenWithdrawCommandHasInvalidParameters() {
        assertThrows(IllegalArgumentException.class, () -> 
            new WithdrawMoneyUseCase.WithdrawCommand("  ", new BigDecimal("50.00"), "USER-1")
        );
        assertThrows(IllegalArgumentException.class, () -> 
            new WithdrawMoneyUseCase.WithdrawCommand("ACC1234567", new BigDecimal("-10.00"), "USER-1")
        );
        assertThrows(IllegalArgumentException.class, () -> 
            new WithdrawMoneyUseCase.WithdrawCommand("ACC1234567", new BigDecimal("50.00"), "")
        );
    }

    @Test
    void shouldCreateValidCreateAccountCommand() {
        CreateAccountUseCase.CreateAccountCommand command = new CreateAccountUseCase.CreateAccountCommand("USER-1");
        assertEquals("USER-1", command.requesterId());
    }

    @Test
    void shouldThrowExceptionWhenCreateAccountCommandHasBlankRequester() {
        assertThrows(IllegalArgumentException.class, () -> 
            new CreateAccountUseCase.CreateAccountCommand("  ")
        );
    }

    @Test
    void shouldCreateValidRegisterUserCommand() {
        RegisterUserUseCase.RegisterUserCommand command = new RegisterUserUseCase.RegisterUserCommand("Alice", "Smith");
        assertEquals("Alice", command.name());
        assertEquals("Smith", command.surname());
    }

    @Test
    void shouldThrowExceptionWhenRegisterUserCommandHasInvalidParameters() {
        assertThrows(IllegalArgumentException.class, () -> 
            new RegisterUserUseCase.RegisterUserCommand(" ", "Smith")
        );
        assertThrows(IllegalArgumentException.class, () -> 
            new RegisterUserUseCase.RegisterUserCommand("Alice", "")
        );
    }
}
