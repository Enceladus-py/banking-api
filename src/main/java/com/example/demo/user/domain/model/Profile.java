package com.example.demo.user.domain.model;

import java.util.UUID;
import lombok.Getter;

/**
 * Domain model representing a user's profile.
 */
@Getter
public class Profile {
    private final String id;
    private String name;
    private String surname;

    /**
     * Creates a new profile with a generated ID.
     *
     * @param name the user's first name
     * @param surname the user's last name
     */
    public Profile(String name, String surname) {
        this(UUID.randomUUID().toString(), name, surname);
    }

    /**
     * Re-hydrates a profile from the database.
     *
     * @param id the profile ID
     * @param name the user's first name
     * @param surname the user's last name
     */
    public Profile(String id, String name, String surname) {
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
