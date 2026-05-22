package com.example.demo.application.port.in;

import com.example.demo.domain.model.User;

public interface RegisterUserUseCase {
    User registerUser(RegisterUserCommand command);

    record RegisterUserCommand(String name, String surname) {
    }
}