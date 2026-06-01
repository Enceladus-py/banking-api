package com.example.demo.application.port.in;

import com.example.demo.domain.model.User;

public interface RegisterUserUseCase {
    User registerUser(RegisterUserCommand command);

    record RegisterUserCommand(String name, String surname) {
        public RegisterUserCommand {
            java.util.Objects.requireNonNull(name, "Name is required");
            if (name.trim().isBlank()) {
                throw new IllegalArgumentException("Name cannot be blank");
            }
            java.util.Objects.requireNonNull(surname, "Surname is required");
            if (surname.trim().isBlank()) {
                throw new IllegalArgumentException("Surname cannot be blank");
            }
        }
    }
}