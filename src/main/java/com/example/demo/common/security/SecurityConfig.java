package com.example.demo.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Global Security Configuration.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	/**
	 * Default constructor.
	 */
	public SecurityConfig() {
	}

	/**
	 * Password encoder bean.
	 *
	 * @return the password encoder
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/**
	 * Configures the security filter chain.
	 *
	 * @param http
	 *            the HttpSecurity context
	 * @return the configured security filter chain
	 * @throws Exception
	 *             if an error occurs
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable).authorizeHttpRequests(auth -> auth
				// Temporarily permit all to keep existing tests passing while we incrementally implement JWT
				.anyRequest().permitAll());
		return http.build();
	}
}
