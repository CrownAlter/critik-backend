package com.application.critik.controllers;

import com.application.critik.entities.Artwork;
import com.application.critik.entities.User;
import com.application.critik.services.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Search", description = "Search for users and artworks")
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
    @Operation(
            summary = "Search users",
            description = "Search for users by username or display name. Case-insensitive partial matching."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved search results")
    })
    @GetMapping("/users")
    public ResponseEntity<List<User>> searchUsers(
            @Parameter(description = "Search query", required = true, example = "john")
            @RequestParam String q) {
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
    @Operation(
            summary = "Search artworks",
            description = "Search for artworks by title, location, or tags. All parameters are optional. Multiple criteria use AND logic."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved search results")
    })
    @GetMapping("/artworks")
    public ResponseEntity<List<Artwork>> searchArtworks(
            @Parameter(description = "Search in artwork titles", example = "sunset")
            @RequestParam(required = false) String title,
            @Parameter(description = "Search in location names", example = "paris")
            @RequestParam(required = false) String location,
            @Parameter(description = "Search in tags", example = "abstract")
            @RequestParam(required = false) String tags) {
        return ResponseEntity.ok(searchService.searchArtworks(title, location, tags));
    }
}
