package com.application.critik.services;

import com.application.critik.entities.Artwork;
import com.application.critik.entities.User;
import com.application.critik.exceptions.ResourceNotFoundException;
import com.application.critik.exceptions.UnauthorizedException;
import com.application.critik.repositories.ArtworkRepository;
import com.application.critik.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Service for managing artwork posts.
 * 
 * Features:
 * - Upload artworks with images, location, artist attribution, and tags
 * - Update artwork details (only by owner)
 * - Delete artworks (only by owner)
 * - Get personalized feed based on followed users
 * - Public feed for non-authenticated users
 * 
 * Security:
 * - Only artwork owners can update or delete their posts
 * - File uploads are validated and stored with UUID prefixes
 */
@Service
public class ArtworkService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    private ArtworkRepository artworkRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private com.application.critik.repositories.CommentRepository commentRepository;
    @Autowired
    private com.application.critik.repositories.ReactionRepository reactionRepository;
    @Autowired
    private ArtworkRevisionService artworkRevisionService;
    @Autowired
    private UserBlockService userBlockService;

    // Allowed image file extensions
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(
            Arrays.asList("jpg", "jpeg", "png", "gif", "webp"));

    /**
     * Gets all artworks (public feed) with pagination.
     * Excludes blocked users if userId is provided.
     */
    public org.springframework.data.domain.Page<Artwork> getAllArtworks(
            org.springframework.data.domain.Pageable pageable, Long userId) {
        if (userId != null) {
            List<Long> blockedUserIds = userBlockService.getAllBlockedUserIds(userId);
            return artworkRepository.findPublicFeedExcludingBlocked(blockedUserIds, pageable);
        }
        return artworkRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    /**
     * Gets artworks by a specific user.
     */
    public List<Artwork> getArtworksByUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));
        return artworkRepository.findByUser(user);
    }

    /**
     * Gets a single artwork by ID.
     */
    public Artwork getArtworkById(Long artworkId) {
        return artworkRepository.findById(artworkId)
                .orElseThrow(() -> new ResourceNotFoundException("Artwork", artworkId));
    }

    /**
     * Uploads a new artwork.
     * 
     * @param username       Username of the uploader
     * @param file           Image file to upload
     * @param title          Artwork title
     * @param artistName     Name of the original artist
     * @param locationName   Location where artwork was found/displayed
     * @param lat            Latitude coordinate (optional)
     * @param lon            Longitude coordinate (optional)
     * @param interpretation User's interpretation/description
     * @param tags           Comma-separated tags
     * @return Created artwork entity
     */
    public Artwork uploadArtwork(String username, MultipartFile file, String title, String artistName,
            String locationName, Double lat, Double lon,
            String interpretation, String tags) throws IOException {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));

        // Validate file
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is required");
        }

        // Validate file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !isValidImageExtension(originalFilename)) {
            throw new IllegalArgumentException("Invalid file type. Allowed: " + ALLOWED_EXTENSIONS);
        }

        // Validate title
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }

        File dir = new File(uploadDir);
        if (!dir.exists())
            dir.mkdirs();

        // Generate unique filename to prevent overwrites and path traversal
        String sanitizedFilename = originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_");
        String fileName = UUID.randomUUID() + "_" + sanitizedFilename;
        Path filePath = Paths.get(uploadDir, fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        Artwork artwork = Artwork.builder()
                .user(user)
                .title(title.trim())
                .artistName(artistName != null ? artistName.trim() : null)
                .imageUrl("/uploads/" + fileName)
                .locationName(locationName != null ? locationName.trim() : null)
                .locationLat(lat)
                .locationLon(lon)
                .interpretation(interpretation != null ? interpretation.trim() : null)
                .tags(tags != null ? tags.trim() : null)
                .build();

        return artworkRepository.save(artwork);
    }

    /**
     * Updates an existing artwork.
     * 
     * SECURITY: Only the artwork owner can update it.
     * FEATURE: Saves revision history before updating.
     * 
     * @param artworkId      ID of artwork to update
     * @param title          New title (optional)
     * @param artistName     New artist name (optional)
     * @param locationName   New location name (optional)
     * @param lat            New latitude (optional)
     * @param lon            New longitude (optional)
     * @param interpretation New interpretation (optional)
     * @param tags           New tags (optional)
     * @return Updated artwork
     */
    @jakarta.transaction.Transactional
    public Artwork updateArtwork(Long artworkId, String title, String artistName,
            String locationName, Double lat, Double lon,
            String interpretation, String tags) {

        Artwork artwork = artworkRepository.findById(artworkId)
                .orElseThrow(() -> new ResourceNotFoundException("Artwork", artworkId));

        // SECURITY: Verify ownership
        verifyOwnership(artwork);

        // Save current state as revision before updating
        artworkRevisionService.saveRevision(artwork);

        // Update fields if provided
        if (title != null && !title.trim().isEmpty()) {
            artwork.setTitle(title.trim());
        }
        if (artistName != null) {
            artwork.setArtistName(artistName.trim().isEmpty() ? null : artistName.trim());
        }
        if (locationName != null) {
            artwork.setLocationName(locationName.trim().isEmpty() ? null : locationName.trim());
        }
        if (lat != null) {
            artwork.setLocationLat(lat);
        }
        if (lon != null) {
            artwork.setLocationLon(lon);
        }
        if (interpretation != null) {
            artwork.setInterpretation(interpretation.trim().isEmpty() ? null : interpretation.trim());
        }
        if (tags != null) {
            artwork.setTags(tags.trim().isEmpty() ? null : tags.trim());
        }

        // Mark as edited
        artwork.setEdited(true);
        artwork.setLastEditedAt(java.time.LocalDateTime.now());

        return artworkRepository.save(artwork);
    }

    /**
     * Deletes an artwork.
     * 
     * SECURITY: Only the artwork owner can delete it.
     * Also deletes the associated image file, comments, and reactions.
     * 
     * @param artworkId ID of artwork to delete
     */
    @jakarta.transaction.Transactional
    public void deleteArtwork(Long artworkId) {
        Artwork artwork = artworkRepository.findById(artworkId)
                .orElseThrow(() -> new ResourceNotFoundException("Artwork", artworkId));

        // SECURITY: Verify ownership
        verifyOwnership(artwork);

        // Delete related entities first to avoid foreign key constraint violations
        // Delete all reactions
        reactionRepository.deleteByArtworkId(artworkId);

        // Delete all comments (including replies due to cascade in Comment entity)
        commentRepository.deleteByArtworkId(artworkId);

        // Delete the image file
        try {
            String imageUrl = artwork.getImageUrl();
            if (imageUrl != null && imageUrl.startsWith("/uploads/")) {
                String fileName = imageUrl.substring("/uploads/".length());
                Path filePath = Paths.get(uploadDir, fileName);
                Files.deleteIfExists(filePath);
            }
        } catch (IOException e) {
            // Log error but don't fail the deletion
            System.err.println("Warning: Could not delete image file: " + e.getMessage());
        }

        // Finally delete the artwork
        artworkRepository.delete(artwork);
    }

    /**
     * Gets personalized feed for authenticated user with pagination.
     * Shows artworks from followed users, excluding blocked users.
     */
    public org.springframework.data.domain.Page<Artwork> getFeedForUser(
            Long userId, org.springframework.data.domain.Pageable pageable) {
        // Get list of blocked user IDs
        List<Long> blockedUserIds = userBlockService.getAllBlockedUserIds(userId);

        // Fetch artworks from followed users, excluding blocked
        return artworkRepository.findFeedForUser(userId, blockedUserIds, pageable);
    }

    /**
     * Gets artworks sorted by popularity (total reactions).
     */
    public org.springframework.data.domain.Page<Artwork> getPopularArtworks(
            org.springframework.data.domain.Pageable pageable) {
        return artworkRepository.findAllByPopularity(pageable);
    }

    /**
     * Gets artworks sorted by controversy (balanced reactions).
     */
    public org.springframework.data.domain.Page<Artwork> getControversialArtworks(
            org.springframework.data.domain.Pageable pageable) {
        return artworkRepository.findAllByControversy(pageable);
    }

    /**
     * Verifies that the current authenticated user owns the artwork.
     * 
     * @param artwork Artwork to verify ownership of
     * @throws UnauthorizedException if user doesn't own the artwork
     */
    private void verifyOwnership(Artwork artwork) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new UnauthorizedException("You must be logged in to perform this action");
        }

        String currentUsername = auth.getName();
        if (!artwork.getUser().getUsername().equalsIgnoreCase(currentUsername)) {
            throw new UnauthorizedException("You can only modify your own artworks");
        }
    }

    /**
     * Validates that the file has an allowed image extension.
     */
    private boolean isValidImageExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot < 0)
            return false;
        String extension = filename.substring(lastDot + 1).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(extension);
    }
}
