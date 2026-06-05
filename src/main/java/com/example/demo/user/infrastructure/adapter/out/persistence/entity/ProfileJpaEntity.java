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

	@Column(name = "mobile_number")
	private String mobileNumber;

	@Column(name = "address")
	private String address;

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
	 * @param mobileNumber
	 *            the user's mobile number
	 * @param address
	 *            the user's address
	 */
	public ProfileJpaEntity(UUID id, UserJpaEntity user, String name, String surname, String mobileNumber,
			String address) {
		this.id = id;
		this.user = user;
		this.name = name;
		this.surname = surname;
		this.mobileNumber = mobileNumber;
		this.address = address;
	}
}
