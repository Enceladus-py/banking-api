package com.example.demo.domain.model;

import java.util.UUID;

import lombok.Getter;

@Getter
public class User {
    private String id;
    private String name;
    private String surname;

    public User(String name, String surname) {
        this(UUID.randomUUID().toString(), name, surname);
    }

    public User(String id, String name, String surname) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (surname == null || surname.isBlank()) {
            throw new IllegalArgumentException("Surname cannot be null or empty");
        }

        this.id = id;
        this.name = name.trim();
        this.surname = surname.trim();
    }
}