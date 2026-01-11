package com.application.critik.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "User entity")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "User ID", example = "1")
    private Long id;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Schema(description = "Unique username", example = "johndoe", required = true)
    private String username;

    @Column(nullable = true)
    @Size(max = 100, message = "Display name cannot exceed 100 characters")
    @Schema(description = "Display name", example = "John Doe")
    private String displayName;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Email address", example = "john@example.com", required = true)
    private String email;

    @Column(nullable = false)
    @JsonIgnore // SECURITY: Never expose password in API responses
    @Schema(hidden = true)
    private String password;

    @Size(max = 500, message = "Bio cannot exceed 500 characters")
    @Schema(description = "User biography", example = "Art enthusiast and photographer")
    private String bio;

}
