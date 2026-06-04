package com.example.demo.account.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * JPA entity representing an available, pre-generated account number in the
 * pool.
 */
@Entity
@Table(name = "available_account_numbers")
@Getter
@Setter
@AllArgsConstructor
public class AvailableAccountNumberJpaEntity {

	/**
	 * Default constructor required by JPA.
	 */
	public AvailableAccountNumberJpaEntity() {
	}

	@Id
	private String accountNumber;
}
