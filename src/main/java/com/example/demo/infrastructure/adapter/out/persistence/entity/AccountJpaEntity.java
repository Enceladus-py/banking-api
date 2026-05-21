package com.example.demo.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountJpaEntity implements Persistable<UUID> {

    @Id
    private UUID id;
    private String name;
    private String surname;
    private String accountNumber;
    private BigDecimal balance;

    @Version
    private Long version; // Managed automatically by Hibernate for optimistic locking

    @Override
    public boolean isNew() {
        // If version is null, it's a brand new insert!
        // This explicitly guides Spring Data Repository without any extra queries.
        return version == null;
    }
}