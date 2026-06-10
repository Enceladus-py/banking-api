package com.fintech.banking.coreapi.user.application.port.out;

import java.util.Optional;

import com.fintech.banking.coreapi.user.domain.model.User;

/**
 * Outbound port interface for user persistence operations.
 */
public interface UserRepository {
	/**
	 * Saves a user domain aggregate.
	 *
	 * @param user
	 *            the user domain model to save
	 * @return the saved user domain model
	 */
	User save(User user);

	/**
	 * Finds a user by their unique ID.
	 *
	 * @param id
	 *            the user ID
	 * @return an Optional containing the User if found, or empty
	 */
	Optional<User> findById(String id);

	/**
	 * Finds a user by their unique email.
	 *
	 * @param email
	 *            the user email
	 * @return an Optional containing the User if found, or empty
	 */
	Optional<User> findByEmail(String email);
}
