package com.example.demo.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class UserJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname;

    @Version
    private Long version;

    public UserJpaEntity(UUID id, String name, String surname, Long version) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.version = version;
    }
}