package com.example.demo.user.application.service;

import com.example.demo.common.application.annotation.TransactionalUseCase;
import com.example.demo.common.domain.exception.EntityNotFoundException;
import com.example.demo.user.application.port.in.UpdateProfileUseCase;
import com.example.demo.user.application.port.out.UserRepository;
import com.example.demo.user.domain.model.User;

/**
 * Service implementation for updating user profile information.
 */
@TransactionalUseCase
public class UpdateProfileService implements UpdateProfileUseCase {

	private final UserRepository userRepository;

	/**
	 * Constructor.
	 *
	 * @param userRepository
	 *            the user repository
	 */
	public UpdateProfileService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public User updateProfile(UpdateProfileCommand command) {
		User user = userRepository.findById(command.userId())
				.orElseThrow(() -> new EntityNotFoundException("User not found with id: " + command.userId()));

		user.updateProfile(command.mobileNumber(), command.address());

		return userRepository.save(user);
	}
}
