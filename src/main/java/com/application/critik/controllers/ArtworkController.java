package com.application.critik.controllers;

import com.application.critik.entities.Artwork;
import com.application.critik.services.ArtworkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * REST controller for artwork operations.
 * 
 * Endpoints:
 * - GET /artworks/feed - Public feed (all artworks)
 * - GET /artworks/feed/{userId} - Personalized feed for user
 * - GET /artworks/my - Current user's artworks (authenticated)
 * - GET /artworks/{id} - Single artwork details
 * - POST /artworks - Upload new artwork (authenticated)
 * - PUT /artworks/{id} - Update artwork (owner only)
 * - DELETE /artworks/{id} - Delete artwork (owner only)
 */
@RestController
@RequestMapping("/artworks")
@Tag(name = "Artworks", description = "Artwork management and feed endpoints")
public class ArtworkController {

    @Autowired
    private ArtworkService artworkService;

    /**
     * Get public feed of all artworks.
     * Accessible to both authenticated and non-authenticated users.
     */
    @Operation(
            summary = "Get public artwork feed",
            description = "Returns all artworks in reverse chronological order. Accessible without authentication."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved artworks")
    })
    @GetMapping("/feed")
    public List<Artwork> getAllArtworks() {
        return artworkService.getAllArtworks();
    }

    /**
     * Get a single artwork by ID.
     */
    @Operation(
            summary = "Get artwork by ID",
            description = "Returns detailed information about a specific artwork"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Artwork found"),
            @ApiResponse(responseCode = "404", description = "Artwork not found", content = @Content)
    })
    @GetMapping("/{artworkId}")
    public Artwork getArtworkById(
            @Parameter(description = "ID of the artwork to retrieve", required = true)
            @PathVariable Long artworkId) {
        return artworkService.getArtworkById(artworkId);
    }

    /**
     * Get current authenticated user's artworks.
     */
    @Operation(
            summary = "Get my artworks",
            description = "Returns all artworks uploaded by the authenticated user",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user's artworks"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping("/my")
    public List<Artwork> getMyArtworks(Authentication authentication) {
        String username = authentication.getName();
        return artworkService.getArtworksByUser(username);
    }

    /**
     * Get personalized feed for a specific user.
     * Shows artworks from users they follow.
     */
    @Operation(
            summary = "Get personalized feed",
            description = "Returns artworks from users that the specified user follows"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved personalized feed"),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @GetMapping("/feed/{userId}")
    public ResponseEntity<List<Artwork>> getFeedForUser(
            @Parameter(description = "ID of the user", required = true)
            @PathVariable Long userId) {
        List<Artwork> feed = artworkService.getFeedForUser(userId);
        return ResponseEntity.ok(feed);
    }

    /**
     * Upload a new artwork.
     * Requires authentication.
     * 
     * @param file Image file (required)
     * @param title Artwork title (required)
     * @param artistName Name of the original artist (optional)
     * @param locationName Location name (optional)
     * @param lat Latitude coordinate (optional)
     * @param lon Longitude coordinate (optional)
     * @param interpretation User's interpretation/description (optional)
     * @param tags Comma-separated tags (optional)
     */
    @Operation(
            summary = "Upload new artwork",
            description = "Upload a new artwork with image and metadata. Requires authentication.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Artwork uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or file", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Artwork> uploadArtwork(
            @Parameter(description = "Image file (JPEG, PNG)", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Artwork title", required = true)
            @RequestParam("title") String title,
            @Parameter(description = "Name of the original artist")
            @RequestParam(value = "artistName", required = false) String artistName,
            @Parameter(description = "Location name")
            @RequestParam(value = "locationName", required = false) String locationName,
            @Parameter(description = "Latitude coordinate")
            @RequestParam(value = "lat", required = false) Double lat,
            @Parameter(description = "Longitude coordinate")
            @RequestParam(value = "lon", required = false) Double lon,
            @Parameter(description = "User's interpretation or description")
            @RequestParam(value = "interpretation", required = false) String interpretation,
            @Parameter(description = "Comma-separated tags")
            @RequestParam(value = "tags", required = false) String tags,
            Principal principal) throws IOException {

        Artwork artwork = artworkService.uploadArtwork(
                principal.getName(), file, title, artistName, locationName, lat, lon, interpretation, tags
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(artwork);
    }

    /**
     * Update an existing artwork.
     * Only the artwork owner can perform this action.
     * 
     * @param artworkId ID of the artwork to update
     * @param body Map containing fields to update (title, artistName, locationName, lat, lon, interpretation, tags)
     */
    @Operation(
            summary = "Update artwork",
            description = "Update artwork metadata. Only the artwork owner can perform this action.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Artwork updated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Not the artwork owner", content = @Content),
            @ApiResponse(responseCode = "404", description = "Artwork not found", content = @Content)
    })
    @PutMapping("/{artworkId}")
    public ResponseEntity<Artwork> updateArtwork(
            @Parameter(description = "ID of the artwork to update", required = true)
            @PathVariable Long artworkId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Fields to update",
                    content = @Content(
                            schema = @Schema(example = "{\"title\": \"Updated Title\", \"interpretation\": \"New interpretation\", \"tags\": \"modern,abstract\"}")
                    )
            )
            @RequestBody Map<String, Object> body) {

        String title = body.get("title") != null ? (String) body.get("title") : null;
        String artistName = body.get("artistName") != null ? (String) body.get("artistName") : null;
        String locationName = body.get("locationName") != null ? (String) body.get("locationName") : null;
        Double lat = body.get("lat") != null ? ((Number) body.get("lat")).doubleValue() : null;
        Double lon = body.get("lon") != null ? ((Number) body.get("lon")).doubleValue() : null;
        String interpretation = body.get("interpretation") != null ? (String) body.get("interpretation") : null;
        String tags = body.get("tags") != null ? (String) body.get("tags") : null;

        Artwork updated = artworkService.updateArtwork(artworkId, title, artistName, locationName, lat, lon, interpretation, tags);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete an artwork.
     * Only the artwork owner can perform this action.
     * Also deletes the associated image file.
     * 
     * @param artworkId ID of the artwork to delete
     */
    @Operation(
            summary = "Delete artwork",
            description = "Delete an artwork and its associated image file. Only the artwork owner can perform this action.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Artwork deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Not the artwork owner", content = @Content),
            @ApiResponse(responseCode = "404", description = "Artwork not found", content = @Content)
    })
    @DeleteMapping("/{artworkId}")
    public ResponseEntity<Map<String, String>> deleteArtwork(
            @Parameter(description = "ID of the artwork to delete", required = true)
            @PathVariable Long artworkId) {
        artworkService.deleteArtwork(artworkId);
        return ResponseEntity.ok(Map.of("message", "Artwork deleted successfully"));
    }
}
