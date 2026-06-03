package com.example.demo.user.application.service;

import com.example.demo.common.application.annotation.TransactionalUseCase;
import com.example.demo.common.domain.exception.EntityNotFoundException;
import com.example.demo.user.application.port.in.GetUserUseCase;
import com.example.demo.user.application.port.in.RegisterUserUseCase;
import com.example.demo.user.application.port.out.UserRepository;
import com.example.demo.user.domain.model.User;

/**
 * Service class implementing use cases for user registration and retrieval.
 */
@TransactionalUseCase
public class UserService implements RegisterUserUseCase, GetUserUseCase {

	private final UserRepository userRepository;

	/**
	 * Constructs a new UserService with the specified user repository.
	 *
	 * @param userRepository
	 *            the user repository outbound port
	 */
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
