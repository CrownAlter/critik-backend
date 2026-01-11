package com.application.critik.exceptions;

/**
 * Exception thrown when a user attempts to perform an action they are not authorized for.
 * Results in HTTP 403 Forbidden response.
 * 
 * Use cases:
 * - Editing another user's profile
 * - Deleting another user's artwork
 * - Performing actions that require ownership verification
 */
public class UnauthorizedException extends RuntimeException {
    
    public UnauthorizedException(String message) {
        super(message);
    }
    
    public UnauthorizedException() {
        super("You are not authorized to perform this action");
    }
}
