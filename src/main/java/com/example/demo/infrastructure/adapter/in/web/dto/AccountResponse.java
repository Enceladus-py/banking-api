package com.example.demo.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;

public record AccountResponse(String id, String ownerId, String accountNumber, BigDecimal balance) {
}
