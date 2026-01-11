package com.application.critik.security;

import com.application.critik.entities.User;
import com.application.critik.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


import java.util.ArrayList;

/**
 * Custom UserDetailsService implementation for Spring Security.
 * 
 * Loads user details from the database for authentication and authorization.
 * Required by Spring Security to authenticate users during login and
 * JWT token validation.
 * 
 * Currently implements basic authentication without roles/authorities.
 * Future enhancement: Add role-based authorization (ADMIN, USER, etc.)
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Load user details by username for authentication.
     * 
     * Called by Spring Security during:
     * - Login authentication
     * - JWT token validation
     * 
     * @param username Username to load
     * @return UserDetails object containing user credentials and authorities
     * @throws UsernameNotFoundException if user not found in database
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        // Convert our User entity to Spring Security UserDetails
        // Empty authorities list - currently no role-based authorization
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(), 
                user.getPassword(), 
                new ArrayList<>() // No authorities/roles yet
        );
    }
}
