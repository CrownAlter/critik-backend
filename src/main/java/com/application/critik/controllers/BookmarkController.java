package com.application.critik.controllers;

import com.application.critik.dto.ArtworkDto;
import com.application.critik.dto.PagedResponse;
import com.application.critik.services.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for bookmark/save functionality.
 * Allows users to save artworks to their profile for later viewing.
 */
@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
@CrossOrigin(origins = "${cors.allowed-origins}")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    /**
     * Bookmark an artwork.
     * POST /api/bookmarks/{artworkId}
     */
    @PostMapping("/{artworkId}")
    public ResponseEntity<Map<String, String>> bookmarkArtwork(
            @PathVariable Long artworkId,
            Authentication authentication) {

        Long userId = getUserIdFromAuth(authentication);
        bookmarkService.bookmarkArtwork(userId, artworkId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Artwork bookmarked successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Remove a bookmark.
     * DELETE /api/bookmarks/{artworkId}
     */
    @DeleteMapping("/{artworkId}")
    public ResponseEntity<Map<String, String>> unbookmarkArtwork(
            @PathVariable Long artworkId,
            Authentication authentication) {

        Long userId = getUserIdFromAuth(authentication);
        bookmarkService.unbookmarkArtwork(userId, artworkId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Bookmark removed successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Check if an artwork is bookmarked.
     * GET /api/bookmarks/{artworkId}/status
     */
    @GetMapping("/{artworkId}/status")
    public ResponseEntity<Map<String, Boolean>> checkBookmarkStatus(
            @PathVariable Long artworkId,
            Authentication authentication) {

        Long userId = getUserIdFromAuth(authentication);
        boolean isBookmarked = bookmarkService.isBookmarked(userId, artworkId);

        Map<String, Boolean> response = new HashMap<>();
        response.put("isBookmarked", isBookmarked);
        return ResponseEntity.ok(response);
    }

    /**
     * Get user's bookmarked artworks (paginated).
     * GET /api/bookmarks?page=0&size=20
     */
    @GetMapping
    public ResponseEntity<PagedResponse<ArtworkDto>> getUserBookmarks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        Long userId = getUserIdFromAuth(authentication);
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<ArtworkDto> bookmarks = bookmarkService.getUserBookmarks(userId, pageable);

        return ResponseEntity.ok(bookmarks);
    }

    /**
     * Helper method to extract user ID from authentication.
     */
    private Long getUserIdFromAuth(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        // Assuming the authentication principal contains the user ID
        // This may need adjustment based on your JWT implementation
        return Long.parseLong(authentication.getName());
    }
}
