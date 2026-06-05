package com.example.demo.user.infrastructure.adapter.out.persistence.entity;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * JPA entity representing a user in the database.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
public class UserJpaEntity {

	/**
	 * Default constructor required by JPA.
	 */
	public UserJpaEntity() {
	}

	@Id
	private UUID id;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false)
	private String password;

	@OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private ProfileJpaEntity profile;

	@Version
	private Long version;

	/**
	 * Constructs a UserJpaEntity with all attributes.
	 *
	 * @param id
	 *            the user ID
	 * @param email
	 *            the user's email
	 * @param password
	 *            the user's hashed password
	 * @param version
	 *            the database version
	 */
	public UserJpaEntity(UUID id, String email, String password, Long version) {
		this.id = id;
		this.email = email;
		this.password = password;
		this.version = version;
	}
}
