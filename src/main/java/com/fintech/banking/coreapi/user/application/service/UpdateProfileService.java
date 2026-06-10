package com.fintech.banking.coreapi.user.application.service;

import com.fintech.banking.coreapi.common.application.annotation.TransactionalUseCase;
import com.fintech.banking.coreapi.common.domain.exception.EntityNotFoundException;
import com.fintech.banking.coreapi.user.application.port.in.UpdateProfileUseCase;
import com.fintech.banking.coreapi.user.application.port.out.UserRepository;
import com.fintech.banking.coreapi.user.domain.model.User;

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
