package com.example.demo.infrastructure.adapter.out.persistence.entity;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "account", uniqueConstraints = @UniqueConstraint(name = "uq_account_number", columnNames = "accountNumber"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountJpaEntity {

	@Id
	private UUID id;
	private String accountNumber;
	private BigDecimal balance;

	@Column(name = "owner_id", nullable = false)
	private String ownerId;

	@Version
	private Long version;
}
