package com.application.critik.controllers;

import com.application.critik.entities.Artwork;
import com.application.critik.services.ArtworkService;
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
public class ArtworkController {

    @Autowired
    private ArtworkService artworkService;

    /**
     * Get public feed of all artworks.
     * Accessible to both authenticated and non-authenticated users.
     */
    @GetMapping("/feed")
    public List<Artwork> getAllArtworks() {
        return artworkService.getAllArtworks();
    }

    /**
     * Get a single artwork by ID.
     */
    @GetMapping("/{artworkId}")
    public Artwork getArtworkById(@PathVariable Long artworkId) {
        return artworkService.getArtworkById(artworkId);
    }

    /**
     * Get current authenticated user's artworks.
     */
    @GetMapping("/my")
    public List<Artwork> getMyArtworks(Authentication authentication) {
        String username = authentication.getName();
        return artworkService.getArtworksByUser(username);
    }

    /**
     * Get personalized feed for a specific user.
     * Shows artworks from users they follow.
     */
    @GetMapping("/feed/{userId}")
    public ResponseEntity<List<Artwork>> getFeedForUser(@PathVariable Long userId) {
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
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Artwork> uploadArtwork(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "artistName", required = false) String artistName,
            @RequestParam(value = "locationName", required = false) String locationName,
            @RequestParam(value = "lat", required = false) Double lat,
            @RequestParam(value = "lon", required = false) Double lon,
            @RequestParam(value = "interpretation", required = false) String interpretation,
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
    @PutMapping("/{artworkId}")
    public ResponseEntity<Artwork> updateArtwork(
            @PathVariable Long artworkId,
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
    @DeleteMapping("/{artworkId}")
    public ResponseEntity<Map<String, String>> deleteArtwork(@PathVariable Long artworkId) {
        artworkService.deleteArtwork(artworkId);
        return ResponseEntity.ok(Map.of("message", "Artwork deleted successfully"));
    }
}
