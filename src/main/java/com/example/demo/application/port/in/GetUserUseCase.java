package com.example.demo.application.port.in;

import com.example.demo.domain.model.User;

public interface GetUserUseCase {
    User getUserById(String userId);
}