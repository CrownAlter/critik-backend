package com.application.critik;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Critik Application - Art Review and Discussion Platform
 * 
 * A social media-like backend for reviewing and discussing art.
 * 
 * Features:
 * - JWT-based authentication with refresh tokens
 * - Artwork posting with artist attribution and location
 * - Nested comments and discussions
 * - Follow system for personalized feeds
 * - AGREE/DISAGREE reactions
 * - Rate limiting for security
 */
@SpringBootApplication
@EnableScheduling // Enable scheduled tasks for refresh token cleanup
public class CritikApplication {

	public static void main(String[] args) {
		SpringApplication.run(CritikApplication.class, args);
	}

}
