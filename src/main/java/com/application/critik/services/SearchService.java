package com.application.critik.services;

import com.application.critik.entities.Artwork;
import com.application.critik.entities.User;
import com.application.critik.repositories.ArtworkRepository;
import com.application.critik.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SearchService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArtworkRepository artworkRepository;

    /**
     * Search users by username or display name (paginated).
     */
    public Page<User> searchUsers(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return Page.empty(pageable);
        }

        // For now, search by username only (can be enhanced with custom query)
        return userRepository.findByUsernameContainingIgnoreCase(query, pageable);
    }

    /**
     * Search artworks by title, location, or tags (paginated).
     * If all parameters are null/blank, returns all artworks.
     */
    public Page<Artwork> searchArtworks(String title, String location, String tags, Pageable pageable) {

        // If title is provided, search by title
        if (title != null && !title.isBlank()) {
            return artworkRepository.findByTitleContainingIgnoreCase(title, pageable);
        }

        // If location is provided, search by location
        if (location != null && !location.isBlank()) {
            return artworkRepository.findByLocationNameContainingIgnoreCase(location, pageable);
        }

        // If tags is provided, search by tags
        if (tags != null && !tags.isBlank()) {
            return artworkRepository.findByTagsContainingIgnoreCase(tags, pageable);
        }

        // If no search criteria, return all artworks
        return artworkRepository.findAllByOrderByCreatedAtDesc(pageable);
    }
}
