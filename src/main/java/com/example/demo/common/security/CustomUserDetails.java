package com.example.demo.common.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

/**
 * Custom implementation of Spring Security's User that includes the
 * application's user ID.
 */
public class CustomUserDetails extends User {

	/**
	 * The application's unique user ID.
	 */
	private final String id;

	/**
	 * Constructs a new CustomUserDetails.
	 *
	 * @param id
	 *            the user ID
	 * @param username
	 *            the username (email)
	 * @param password
	 *            the password
	 * @param authorities
	 *            the authorities
	 */
	public CustomUserDetails(String id, String username, String password,
			Collection<? extends GrantedAuthority> authorities) {
		super(username, password, authorities);
		this.id = id;
	}

	/**
	 * Returns the user ID.
	 *
	 * @return the user ID
	 */
	public String getId() {
		return id;
	}
}
