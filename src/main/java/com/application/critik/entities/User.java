package com.application.critik.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * User entity representing registered users of the Critik platform.
 * 
 * Security considerations:
 * - Password is excluded from JSON serialization via @JsonIgnore
 * - Email is validated for proper format
 * - Username must be unique and between 3-50 characters
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @Column(nullable = true)
    @Size(max = 100, message = "Display name cannot exceed 100 characters")
    private String displayName;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Column(nullable = false)
    @JsonIgnore // SECURITY: Never expose password in API responses
    private String password;

    @Size(max = 500, message = "Bio cannot exceed 500 characters")
    private String bio;

}
