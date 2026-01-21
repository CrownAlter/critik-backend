package com.application.critik.services;

import com.application.critik.dto.ArtworkDto;
import com.application.critik.dto.PagedResponse;
import com.application.critik.entities.Artwork;
import com.application.critik.entities.Bookmark;
import com.application.critik.entities.User;
import com.application.critik.exceptions.DuplicateResourceException;
import com.application.critik.exceptions.ResourceNotFoundException;
import com.application.critik.mappers.ArtworkMapper;
import com.application.critik.repositories.ArtworkRepository;
import com.application.critik.repositories.BookmarkRepository;
import com.application.critik.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing user bookmarks (saved artworks).
 */
@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final ArtworkRepository artworkRepository;
    private final UserRepository userRepository;
    private final ArtworkMapper artworkMapper;

    /**
     * Bookmark an artwork for a user.
     * 
     * @param userId    User ID
     * @param artworkId Artwork ID
     * @throws ResourceNotFoundException  if user or artwork not found
     * @throws DuplicateResourceException if already bookmarked
     */
    @Transactional
    public void bookmarkArtwork(Long userId, Long artworkId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Artwork artwork = artworkRepository.findById(artworkId)
                .orElseThrow(() -> new ResourceNotFoundException("Artwork not found"));

        // Check if already bookmarked
        if (bookmarkRepository.existsByUserAndArtwork(user, artwork)) {
            throw new DuplicateResourceException("Artwork already bookmarked");
        }

        Bookmark bookmark = Bookmark.builder()
                .user(user)
                .artwork(artwork)
                .build();

        bookmarkRepository.save(bookmark);
    }

    /**
     * Remove a bookmark.
     * 
     * @param userId    User ID
     * @param artworkId Artwork ID
     * @throws ResourceNotFoundException if user or artwork not found
     */
    @Transactional
    public void unbookmarkArtwork(Long userId, Long artworkId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Artwork artwork = artworkRepository.findById(artworkId)
                .orElseThrow(() -> new ResourceNotFoundException("Artwork not found"));

        bookmarkRepository.deleteByUserAndArtwork(user, artwork);
    }

    /**
     * Check if a user has bookmarked an artwork.
     * 
     * @param userId    User ID
     * @param artworkId Artwork ID
     * @return true if bookmarked, false otherwise
     */
    public boolean isBookmarked(Long userId, Long artworkId) {
        User user = userRepository.findById(userId).orElse(null);
        Artwork artwork = artworkRepository.findById(artworkId).orElse(null);

        if (user == null || artwork == null) {
            return false;
        }

        return bookmarkRepository.existsByUserAndArtwork(user, artwork);
    }

    /**
     * Get all bookmarked artworks for a user (paginated).
     * 
     * @param userId   User ID
     * @param pageable Pagination parameters
     * @return Paginated list of bookmarked artworks
     * @throws ResourceNotFoundException if user not found
     */
    public PagedResponse<ArtworkDto> getUserBookmarks(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Page<Bookmark> bookmarkPage = bookmarkRepository.findByUserOrderByCreatedAtDesc(user, pageable);

        Page<ArtworkDto> artworkDtoPage = bookmarkPage.map(bookmark -> artworkMapper.toDto(bookmark.getArtwork()));

        return PagedResponse.<ArtworkDto>builder()
                .content(artworkDtoPage.getContent())
                .page(artworkDtoPage.getNumber())
                .size(artworkDtoPage.getSize())
                .totalElements(artworkDtoPage.getTotalElements())
                .totalPages(artworkDtoPage.getTotalPages())
                .last(artworkDtoPage.isLast())
                .first(artworkDtoPage.isFirst())
                .build();
    }
}
