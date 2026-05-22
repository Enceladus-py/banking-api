package com.example.demo.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class UserJpaEntity implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname;

    @Transient
    private boolean isNew = true;

    public UserJpaEntity(UUID id, String name, String surname) {
        this.id = id;
        this.name = name;
        this.surname = surname;
    }

    @Override
    public boolean isNew() {
        return this.isNew;
    }

    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }

    @PrePersist
    void markNotNewAfterInsert() {
        this.isNew = false;
    }
}