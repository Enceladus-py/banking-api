package com.example.demo.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterUserRequest(@NotBlank(message = "Name is required") String name,
		@NotBlank(message = "Surname is required") String surname) {
}
