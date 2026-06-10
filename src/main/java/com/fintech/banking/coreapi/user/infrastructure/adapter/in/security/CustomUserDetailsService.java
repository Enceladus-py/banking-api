package com.fintech.banking.coreapi.user.infrastructure.adapter.in.security;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.fintech.banking.coreapi.common.infrastructure.security.CustomUserDetails;
import com.fintech.banking.coreapi.user.application.port.out.UserRepository;
import com.fintech.banking.coreapi.user.domain.model.User;

/**
 * Custom implementation of {@link UserDetailsService} that loads user details
 * from the domain repository.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	/**
	 * Constructs the CustomUserDetailsService.
	 *
	 * @param userRepository
	 *            the user repository
	 */
	public CustomUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	/**
	 * Locates the user based on the username (email).
	 *
	 * @param username
	 *            the email identifying the user whose data is required.
	 * @return a fully populated user record
	 * @throws UsernameNotFoundException
	 *             if the user could not be found or the user has no
	 *             GrantedAuthority
	 */
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByEmail(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

		return new CustomUserDetails(user.getId(), user.getEmail(), user.getPassword(),
				Collections.singletonList(new SimpleGrantedAuthority("USER")));
	}
}
