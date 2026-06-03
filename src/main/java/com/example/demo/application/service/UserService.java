package com.example.demo.application.service;

import com.example.demo.application.annotation.TransactionalUseCase;
import com.example.demo.application.port.in.GetUserUseCase;
import com.example.demo.application.port.in.RegisterUserUseCase;
import com.example.demo.application.port.out.UserRepository;
import com.example.demo.domain.exception.EntityNotFoundException;
import com.example.demo.domain.model.User;

@TransactionalUseCase
public class UserService implements RegisterUserUseCase, GetUserUseCase {

	private final UserRepository userRepository;

	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public User registerUser(RegisterUserCommand command) {
		// The Domain model generates its own UUID and trims the strings
		User user = new User(command.name(), command.surname());
		return userRepository.save(user);
	}

	@Override
	public User getUserById(String userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
	}
}
