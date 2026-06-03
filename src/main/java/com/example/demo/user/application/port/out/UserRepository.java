package com.example.demo.user.application.port.out;

import java.util.Optional;

import com.example.demo.user.domain.model.User;

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
}
