package com.example.demo.user.application.service;

import org.springframework.security.crypto.password.PasswordEncoder;

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
	private final PasswordEncoder passwordEncoder;

	/**
	 * Constructs a new UserService with the specified user repository and password encoder.
	 *
	 * @param userRepository
	 *            the user repository outbound port
	 * @param passwordEncoder
	 *            the Spring Security password encoder
	 */
	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public User registerUser(RegisterUserCommand command) {
		if (userRepository.findByEmail(command.email()).isPresent()) {
			throw new IllegalArgumentException("Email is already in use");
		}
		String encodedPassword = passwordEncoder.encode(command.password());
		User user = new User(command.email(), encodedPassword, command.name(), command.surname());
		return userRepository.save(user);
	}

	@Override
	public User getUserById(String userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
	}
}
