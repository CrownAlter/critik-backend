package com.application.critik.controllers;

import com.application.critik.entities.Artwork;
import com.application.critik.entities.User;
import com.application.critik.services.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for search operations.
 * 
 * Endpoints:
 * - GET /search/users - Search users by username or display name
 * - GET /search/artworks - Search artworks by title, location, or tags
 * 
 * All search endpoints are publicly accessible (no authentication required).
 * Search is case-insensitive and uses partial matching.
 */
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    @Autowired
    private SearchService searchService;

    /**
     * Search for users by username or display name.
     * Case-insensitive partial match search.
     * 
     * @param q Search query string
     * @return List of users matching the query
     */
    @GetMapping("/users")
    public ResponseEntity<List<User>> searchUsers(@RequestParam String q) {
        return ResponseEntity.ok(searchService.searchUsers(q));
    }

    /**
     * Search for artworks by multiple criteria.
     * All parameters are optional; at least one should be provided.
     * Multiple criteria are combined with AND logic.
     * 
     * @param title Search in artwork titles (optional)
     * @param location Search in location names (optional)
     * @param tags Search in tags (optional)
     * @return List of artworks matching the criteria
     */
    @GetMapping("/artworks")
    public ResponseEntity<List<Artwork>> searchArtworks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String tags) {
        return ResponseEntity.ok(searchService.searchArtworks(title, location, tags));
    }
}
