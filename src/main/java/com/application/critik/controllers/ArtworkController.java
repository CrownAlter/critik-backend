package com.application.critik.controllers;

import com.application.critik.dto.ArtworkDto;
import com.application.critik.dto.PagedResponse;
import com.application.critik.entities.Artwork;
import com.application.critik.mappers.ArtworkMapper;
import com.application.critik.services.ArtworkService;
import com.application.critik.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        @Autowired
        private ArtworkMapper artworkMapper;
        @Autowired
        private UserRepository userRepository;

        /**
         * Get public feed of all artworks.
         * Accessible to both authenticated and non-authenticated users.
         * Supports pagination.
         */
        @GetMapping("/feed")
        public ResponseEntity<PagedResponse<ArtworkDto>> getAllArtworks(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        Authentication authentication) {

                Pageable pageable = PageRequest.of(page, size);
                Long userId = getUserIdFromAuth(authentication);

                Page<Artwork> artworks = artworkService.getAllArtworks(pageable, userId);

                List<ArtworkDto> content = artworks.getContent().stream()
                                .map(artwork -> artworkMapper.toDto(artwork, userId))
                                .collect(Collectors.toList());

                PagedResponse<ArtworkDto> response = new PagedResponse<>(
                                content, artworks.getNumber(), artworks.getSize(),
                                artworks.getTotalElements(), artworks.getTotalPages(),
                                artworks.isLast(), artworks.isFirst());

                return ResponseEntity.ok(response);
        }

        /**
         * Get a single artwork by ID.
         */
        @GetMapping("/{artworkId}")
        public ResponseEntity<ArtworkDto> getArtworkById(
                        @PathVariable Long artworkId,
                        Authentication authentication) {

                Long userId = getUserIdFromAuth(authentication);
                Artwork artwork = artworkService.getArtworkById(artworkId);
                return ResponseEntity.ok(artworkMapper.toDto(artwork, userId));
        }

        /**
         * Get current authenticated user's artworks.
         */
        @GetMapping("/my")
        public ResponseEntity<List<ArtworkDto>> getMyArtworks(Authentication authentication) {
                String username = authentication.getName();
                Long userId = getUserIdFromAuth(authentication);
                List<Artwork> artworks = artworkService.getArtworksByUser(username);

                List<ArtworkDto> dtos = artworks.stream()
                                .map(artwork -> artworkMapper.toDto(artwork, userId))
                                .collect(Collectors.toList());

                return ResponseEntity.ok(dtos);
        }

        /**
         * Get personalized feed for a specific user.
         * Shows artworks from users they follow.
         */
        @GetMapping("/feed/{userId}")
        public ResponseEntity<PagedResponse<ArtworkDto>> getFeedForUser(
                        @PathVariable Long userId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        Authentication authentication) {

                // Security check could be added here to ensure user is requesting their own
                // feed
                // or a public feed logic if applicable. Assuming typical usage is "my feed".
                Pageable pageable = PageRequest.of(page, size);
                Page<Artwork> artworks = artworkService.getFeedForUser(userId, pageable);

                Long currentUserId = getUserIdFromAuth(authentication);

                List<ArtworkDto> content = artworks.getContent().stream()
                                .map(artwork -> artworkMapper.toDto(artwork, currentUserId))
                                .collect(Collectors.toList());

                PagedResponse<ArtworkDto> response = new PagedResponse<>(
                                content, artworks.getNumber(), artworks.getSize(),
                                artworks.getTotalElements(), artworks.getTotalPages(),
                                artworks.isLast(), artworks.isFirst());

                return ResponseEntity.ok(response);
        }

        /**
         * Upload a new artwork.
         * Requires authentication.
         * 
         * @param file           Image file (required)
         * @param title          Artwork title (required)
         * @param artistName     Name of the original artist (optional)
         * @param locationName   Location name (optional)
         * @param lat            Latitude coordinate (optional)
         * @param lon            Longitude coordinate (optional)
         * @param interpretation User's interpretation/description (optional)
         * @param tags           Comma-separated tags (optional)
         */
        @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<ArtworkDto> uploadArtwork(
                        @RequestParam("file") MultipartFile file,
                        @RequestParam("title") String title,
                        @RequestParam(value = "artistName", required = false) String artistName,
                        @RequestParam(value = "locationName", required = false) String locationName,
                        @RequestParam(value = "lat", required = false) Double lat,
                        @RequestParam(value = "lon", required = false) Double lon,
                        @RequestParam(value = "interpretation", required = false) String interpretation,
                        @RequestParam(value = "tags", required = false) String tags,
                        Principal principal,
                        Authentication authentication) throws IOException {

                Artwork artwork = artworkService.uploadArtwork(
                                principal.getName(), file, title, artistName, locationName, lat, lon, interpretation,
                                tags);

                Long userId = getUserIdFromAuth(authentication);
                return ResponseEntity.status(HttpStatus.CREATED).body(artworkMapper.toDto(artwork, userId));
        }

        /**
         * Update an existing artwork.
         * Only the artwork owner can perform this action.
         * 
         * @param artworkId ID of the artwork to update
         * @param body      Map containing fields to update (title, artistName,
         *                  locationName, lat, lon, interpretation, tags)
         */
        @PutMapping("/{artworkId}")
        public ResponseEntity<ArtworkDto> updateArtwork(
                        @PathVariable Long artworkId,
                        @RequestBody Map<String, Object> body,
                        Authentication authentication) {

                String title = body.get("title") != null ? (String) body.get("title") : null;
                String artistName = body.get("artistName") != null ? (String) body.get("artistName") : null;
                String locationName = body.get("locationName") != null ? (String) body.get("locationName") : null;
                Double lat = body.get("lat") != null ? ((Number) body.get("lat")).doubleValue() : null;
                Double lon = body.get("lon") != null ? ((Number) body.get("lon")).doubleValue() : null;
                String interpretation = body.get("interpretation") != null ? (String) body.get("interpretation") : null;
                String tags = body.get("tags") != null ? (String) body.get("tags") : null;

                Artwork updated = artworkService.updateArtwork(artworkId, title, artistName, locationName, lat, lon,
                                interpretation, tags);
                Long userId = getUserIdFromAuth(authentication);
                return ResponseEntity.ok(artworkMapper.toDto(updated, userId));
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

        /**
         * Popularity feed.
         */
        @GetMapping("/popular")
        public ResponseEntity<PagedResponse<ArtworkDto>> getPopularArtworks(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        Authentication authentication) {

                Pageable pageable = PageRequest.of(page, size);
                Page<Artwork> artworks = artworkService.getPopularArtworks(pageable);
                Long userId = getUserIdFromAuth(authentication);

                List<ArtworkDto> content = artworks.getContent().stream()
                                .map(artwork -> artworkMapper.toDto(artwork, userId))
                                .collect(Collectors.toList());

                PagedResponse<ArtworkDto> response = new PagedResponse<>(
                                content, artworks.getNumber(), artworks.getSize(),
                                artworks.getTotalElements(), artworks.getTotalPages(),
                                artworks.isLast(), artworks.isFirst());
                return ResponseEntity.ok(response);
        }

        /**
         * Controversial feed.
         */
        @GetMapping("/controversial")
        public ResponseEntity<PagedResponse<ArtworkDto>> getControversialArtworks(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        Authentication authentication) {

                Pageable pageable = PageRequest.of(page, size);
                Page<Artwork> artworks = artworkService.getControversialArtworks(pageable);
                Long userId = getUserIdFromAuth(authentication);

                List<ArtworkDto> content = artworks.getContent().stream()
                                .map(artwork -> artworkMapper.toDto(artwork, userId))
                                .collect(Collectors.toList());

                PagedResponse<ArtworkDto> response = new PagedResponse<>(
                                content, artworks.getNumber(), artworks.getSize(),
                                artworks.getTotalElements(), artworks.getTotalPages(),
                                artworks.isLast(), artworks.isFirst());
                return ResponseEntity.ok(response);
        }

        /**
         * Helper to get user ID from auth.
         */
        private Long getUserIdFromAuth(Authentication authentication) {
                if (authentication != null && authentication.isAuthenticated()
                                && !"anonymousUser".equals(authentication.getPrincipal())) {
                        String username = authentication.getName();
                        return userRepository.findByUsername(username).map(com.application.critik.entities.User::getId)
                                        .orElse(null);
                }
                return null;
        }

}
