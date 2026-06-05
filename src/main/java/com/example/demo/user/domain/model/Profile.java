package com.example.demo.user.domain.model;

import java.util.UUID;

import lombok.Getter;

/**
 * Domain model representing a user's profile.
 */
@Getter
public class Profile {
	private final String id;
	private String name;
	private String surname;

	private String mobileNumber;
	private String address;

	/**
	 * Creates a new profile with a generated ID.
	 *
	 * @param name
	 *            the user's first name
	 * @param surname
	 *            the user's last name
	 */
	public Profile(String name, String surname) {
		this(UUID.randomUUID().toString(), name, surname, null, null);
	}

	/**
	 * Re-hydrates a profile from the database.
	 *
	 * @param id
	 *            the profile ID
	 * @param name
	 *            the user's first name
	 * @param surname
	 *            the user's last name
	 * @param mobileNumber
	 *            the user's mobile number
	 * @param address
	 *            the user's address
	 */
	public Profile(String id, String name, String surname, String mobileNumber, String address) {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("Name cannot be null or empty");
		}
		if (surname == null || surname.isBlank()) {
			throw new IllegalArgumentException("Surname cannot be null or empty");
		}
		this.id = id;
		this.name = name.trim();
		this.surname = surname.trim();
		this.mobileNumber = mobileNumber != null ? mobileNumber.trim() : null;
		this.address = address != null ? address.trim() : null;
	}

	/**
	 * Updates the profile with new values. Only updates non-null fields (partial
	 * update).
	 *
	 * @param mobileNumber
	 *            the new mobile number (or null to ignore)
	 * @param address
	 *            the new address (or null to ignore)
	 */
	public void update(String mobileNumber, String address) {
		if (mobileNumber != null) {
			this.mobileNumber = mobileNumber.trim();
		}
		if (address != null) {
			this.address = address.trim();
		}
	}
}
