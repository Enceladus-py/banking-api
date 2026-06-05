package com.example.demo.user.infrastructure.adapter.out.persistence.entity;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * JPA entity representing a user's profile in the database.
 */
@Entity
@Table(name = "profiles")
@Getter
@Setter
public class ProfileJpaEntity {

	/**
	 * Default constructor required by JPA.
	 */
	public ProfileJpaEntity() {
	}

	@Id
	private UUID id;

	@OneToOne
	@JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
	private UserJpaEntity user;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String surname;

	/**
	 * Constructs a ProfileJpaEntity with all attributes.
	 *
	 * @param id
	 *            the profile ID
	 * @param user
	 *            the parent user entity
	 * @param name
	 *            the user's first name
	 * @param surname
	 *            the user's last name
	 */
	public ProfileJpaEntity(UUID id, UserJpaEntity user, String name, String surname) {
		this.id = id;
		this.user = user;
		this.name = name;
		this.surname = surname;
	}
}
