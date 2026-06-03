package com.example.demo.account.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;

public record AccountResponse(String id, String ownerId, String accountNumber, BigDecimal balance) {
}
