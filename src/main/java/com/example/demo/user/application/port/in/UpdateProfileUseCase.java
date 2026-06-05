package com.example.demo.user.application.port.in;

import com.example.demo.user.domain.model.User;

/**
 * Inbound port interface for updating user profile information.
 */
public interface UpdateProfileUseCase {

	/**
	 * Updates the profile of the user identified in the command.
	 *
	 * @param command
	 *            the profile update command
	 * @return the updated user
	 */
	User updateProfile(UpdateProfileCommand command);

	/**
	 * Command containing input details for updating a profile.
	 *
	 * @param userId
	 *            the ID of the user whose profile is being updated
	 * @param mobileNumber
	 *            the new mobile number, or null to remain unchanged
	 * @param address
	 *            the new address, or null to remain unchanged
	 */
	record UpdateProfileCommand(String userId, String mobileNumber, String address) {
		/**
		 * Constructor validating profile update input fields.
		 *
		 * @param userId
		 *            the user ID
		 * @param mobileNumber
		 *            the mobile number
		 * @param address
		 *            the address
		 */
		public UpdateProfileCommand {
			java.util.Objects.requireNonNull(userId, "User ID is required");
			if (userId.trim().isBlank()) {
				throw new IllegalArgumentException("User ID cannot be blank");
			}
		}
	}
}
