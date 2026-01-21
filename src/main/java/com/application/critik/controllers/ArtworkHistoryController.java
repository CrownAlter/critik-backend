package com.application.critik.controllers;

import com.application.critik.dto.ArtworkRevisionDto;
import com.application.critik.services.ArtworkRevisionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for artwork edit history.
 * Allows viewing the revision history of artworks.
 */
@RestController
@RequestMapping("/api/artworks/{artworkId}/history")
@RequiredArgsConstructor
@CrossOrigin(origins = "${cors.allowed-origins}")
public class ArtworkHistoryController {

    private final ArtworkRevisionService artworkRevisionService;

    /**
     * Get edit history for an artwork.
     * GET /api/artworks/{artworkId}/history
     */
    @GetMapping
    public ResponseEntity<List<ArtworkRevisionDto>> getArtworkHistory(
            @PathVariable Long artworkId) {

        List<ArtworkRevisionDto> history = artworkRevisionService.getArtworkHistory(artworkId);
        return ResponseEntity.ok(history);
    }

    /**
     * Get the number of edits for an artwork.
     * GET /api/artworks/{artworkId}/history/count
     */
    @GetMapping("/count")
    public ResponseEntity<Long> getEditCount(
            @PathVariable Long artworkId) {

        long count = artworkRevisionService.getEditCount(artworkId);
        return ResponseEntity.ok(count);
    }
}
