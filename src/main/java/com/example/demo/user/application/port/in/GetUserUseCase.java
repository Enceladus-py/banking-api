package com.example.demo.user.application.port.in;

import com.example.demo.user.domain.model.User;

public interface GetUserUseCase {
	User getUserById(String userId);
}
