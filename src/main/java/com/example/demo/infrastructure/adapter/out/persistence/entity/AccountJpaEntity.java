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
    private String accountNumber;
    private BigDecimal balance;

    @Column(name = "owner_id", nullable = false)
    private String ownerId;

    @Transient
    @Builder.Default
    private boolean isNew = true;

    @Override
    public boolean isNew() {
        return this.isNew;
    }

    // Runs automatically after the entity is loaded from the DB
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }

    // Runs automatically just before the entity is inserted into the DB
    @PrePersist
    void markNotNewAfterInsert() {
        this.isNew = false;
    }
}