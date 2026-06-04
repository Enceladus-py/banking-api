package com.example.demo.account.infrastructure.adapter.out.persistence.entity;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

/**
 * JPA entity representing a bank account in the database.
 */
@Entity
@Table(name = "account", uniqueConstraints = @UniqueConstraint(name = "uq_account_number", columnNames = "account_number"))
@Getter
@Setter
@AllArgsConstructor
@Builder
public class AccountJpaEntity {

	/**
	 * Default constructor required by JPA.
	 */
	public AccountJpaEntity() {
	}

	@Id
	private UUID id;
	private String accountNumber;
	private BigDecimal balance;

	@Column(name = "owner_id", nullable = false)
	private String ownerId;

	@Version
	private Long version;
}
