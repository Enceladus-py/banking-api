package com.example.demo.user.application.port.in;

import com.example.demo.user.domain.model.User;

/**
 * Inbound port interface for registering a new user.
 */
public interface RegisterUserUseCase {
	/**
	 * Registers a user according to the details specified in the command.
	 *
	 * @param command
	 *            the command containing registration details
	 * @return the newly registered user
	 */
	User registerUser(RegisterUserCommand command);

	/**
	 * Command containing input details for registering a new user.
	 *
	 * @param name
	 *            the user name
	 * @param surname
	 *            the user surname
	 */
	record RegisterUserCommand(String name, String surname) {
		/**
		 * Constructor validating name and surname fields.
		 *
		 * @param name
		 *            the user name
		 * @param surname
		 *            the user surname
		 */
		public RegisterUserCommand {
			java.util.Objects.requireNonNull(name, "Name is required");
			if (name.trim().isBlank()) {
				throw new IllegalArgumentException("Name cannot be blank");
			}
			java.util.Objects.requireNonNull(surname, "Surname is required");
			if (surname.trim().isBlank()) {
				throw new IllegalArgumentException("Surname cannot be blank");
			}
		}
	}
}
