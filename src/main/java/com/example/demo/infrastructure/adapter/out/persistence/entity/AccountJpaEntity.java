package com.example.demo.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    private String surname;

    @Column(name = "account_number", unique = true, nullable = false)
    private String accountNumber;

    private BigDecimal balance;
}