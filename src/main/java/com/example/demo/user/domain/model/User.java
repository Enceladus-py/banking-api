package com.example.demo.user.domain.model;

import java.util.UUID;

import lombok.Getter;

/**
 * Domain model representing a user (customer) in the system.
 */
@Getter
public class User {
	private String id;
	private String name;
	private String surname;
	private final Long version;

	/**
	 * Constructor for creating a brand new User.
	 *
	 * @param name
	 *            the user's first name
	 * @param surname
	 *            the user's last name
	 */
	public User(String name, String surname) {
		this(UUID.randomUUID().toString(), name, surname, null);
	}

	/**
	 * Constructor for database re-hydration and mapping.
	 *
	 * @param id
	 *            the user ID
	 * @param name
	 *            the user's first name
	 * @param surname
	 *            the user's last name
	 * @param version
	 *            the database version
	 */
	public User(String id, String name, String surname, Long version) {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("Name cannot be null or empty");
		}
		if (surname == null || surname.isBlank()) {
			throw new IllegalArgumentException("Surname cannot be null or empty");
		}

		this.id = id;
		this.name = name.trim();
		this.surname = surname.trim();
		this.version = version;
	}
}
