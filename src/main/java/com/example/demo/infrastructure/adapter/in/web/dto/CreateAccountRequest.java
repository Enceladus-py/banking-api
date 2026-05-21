package com.example.demo.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateAccountRequest(
                @NotBlank(message = "Name cannot be blank") String name,
                @NotBlank(message = "Surname cannot be blank") String surname) {
}