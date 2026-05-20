package com.example.demo.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;

public record AccountResponse(
        String id,
        String name,
        String surname,
        String accountNumber,
        BigDecimal balance) {
}