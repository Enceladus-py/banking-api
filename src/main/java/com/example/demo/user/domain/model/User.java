package com.example.demo.user.domain.model;

import java.util.UUID;

import lombok.Getter;

/**
 * Domain model representing a user in the system. Acts as the Aggregate Root
 * for User and Profile data.
 */
@Getter
public class User {
	private final String id;
	private String email;
	private String password;
	private Profile profile;
	private final Long version;

	/**
	 * Constructor for creating a brand new User.
	 *
	 * @param email
	 *            the user's email
	 * @param password
	 *            the user's hashed password
	 * @param name
	 *            the user's first name
	 * @param surname
	 *            the user's last name
	 */
	public User(String email, String password, String name, String surname) {
		this(UUID.randomUUID().toString(), email, password, new Profile(name, surname), null);
	}

	/**
	 * Constructor for database re-hydration and mapping.
	 *
	 * @param id
	 *            the user ID
	 * @param email
	 *            the user's email
	 * @param password
	 *            the user's hashed password
	 * @param profile
	 *            the user's profile
	 * @param version
	 *            the database version
	 */
	public User(String id, String email, String password, Profile profile, Long version) {
		if (email == null || email.isBlank()) {
			throw new IllegalArgumentException("Email cannot be null or empty");
		}
		if (password == null || password.isBlank()) {
			throw new IllegalArgumentException("Password cannot be null or empty");
		}
		if (profile == null) {
			throw new IllegalArgumentException("Profile cannot be null");
		}

		this.id = id;
		this.email = email.trim();
		this.password = password;
		this.profile = profile;
		this.version = version;
	}

	/**
	 * Updates the user's profile information.
	 *
	 * @param mobileNumber
	 *            the new mobile number
	 * @param address
	 *            the new address
	 */
	public void updateProfile(String mobileNumber, String address) {
		this.profile.update(mobileNumber, address);
	}
}
