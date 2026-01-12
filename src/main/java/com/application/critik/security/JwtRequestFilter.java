package com.application.critik.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT authentication filter that validates JWT tokens on incoming requests.
 * 
 * This filter:
 * 1. Extracts JWT token from Authorization header (Bearer token)
 * 2. Validates the token and extracts username
 * 3. Loads user details and sets authentication in SecurityContext
 * 4. Skips processing for public endpoints (auth, uploads, public feed)
 * 
 * Extends OncePerRequestFilter to ensure filter executes only once per request.
 * 
 * Order in filter chain: After RateLimitFilter, before authorization checks
 */
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    /**
     * Determines if this filter should be skipped for the current request.
     * Skips public routes that don't require authentication.
     * 
     * @param request The HTTP request
     * @return true if filter should be skipped, false otherwise
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath(); // Excludes context path
        
        // Public prefixes - authentication not required
        if (path.startsWith("/uploads/") || path.startsWith("/auth/")) return true;

        // OpenAPI / Swagger UI
        if (path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui") || path.equals("/swagger-ui.html")) return true;
        
        // Public exact routes
        if (path.equals("/artworks/feed") || path.equals("/favicon.ico")) return true;
        
        // Static assets (if any)
        if (path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/images/")) return true;
        
        return false;
    }

    /**
     * Main filter logic that processes JWT authentication.
     * 
     * Process:
     * 1. Extract Authorization header
     * 2. Parse Bearer token
     * 3. Extract username from token
     * 4. Load user details from database
     * 5. Validate token
     * 6. Set authentication in SecurityContext if valid
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @param filterChain Filter chain to continue processing
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        String username = null;
        String jwt = null;

        // Extract JWT token from Authorization header
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7); // Remove "Bearer " prefix
            username = jwtUtil.extractUsername(jwt);
        }

        // Validate and authenticate if token present and no existing authentication
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(jwt, userDetails)) {
                // Create authentication token
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // Set authentication in SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        
        // Continue filter chain
        filterChain.doFilter(request, response);
    }
}
