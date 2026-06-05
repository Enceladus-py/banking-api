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

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String surname;

	@Version
	private Long version;

	/**
	 * Constructs a UserJpaEntity with all attributes.
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
	public UserJpaEntity(UUID id, String name, String surname, Long version) {
		this.id = id;
		this.name = name;
		this.surname = surname;
		this.version = version;
	}
}
