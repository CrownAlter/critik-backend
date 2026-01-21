package com.application.critik.controllers;

import com.application.critik.dto.ArtworkDto;
import com.application.critik.dto.PagedResponse;
import com.application.critik.dto.UserDto;
import com.application.critik.entities.Artwork;
import com.application.critik.entities.User;
import com.application.critik.mappers.ArtworkMapper;
import com.application.critik.services.SearchService;
import com.application.critik.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

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
        @Autowired
        private ArtworkMapper artworkMapper;
        @Autowired
        private UserRepository userRepository;

        /**
         * Search for users by username or display name.
         * Case-insensitive partial match search.
         * 
         * @param q Search query string
         * @return List of users matching the query
         */
        /**
         * Search for users by username or display name.
         * Case-insensitive partial match search.
         * Supports pagination.
         */
        @GetMapping("/users")
        public ResponseEntity<PagedResponse<UserDto>> searchUsers(
                        @RequestParam String q,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {

                Pageable pageable = PageRequest.of(page, size);
                Page<User> users = searchService.searchUsers(q, pageable);

                List<UserDto> content = users.getContent().stream()
                                .map(user -> UserDto.builder()
                                                .id(user.getId())
                                                .username(user.getUsername())
                                                .displayName(user.getDisplayName())
                                                .email(user.getEmail())
                                                .bio(user.getBio())
                                                .avatarUrl(user.getAvatarUrl())
                                                .bannerUrl(user.getBannerUrl())
                                                .build())
                                .collect(Collectors.toList());

                PagedResponse<UserDto> response = new PagedResponse<>(
                                content, users.getNumber(), users.getSize(),
                                users.getTotalElements(), users.getTotalPages(),
                                users.isLast(), users.isFirst());

                return ResponseEntity.ok(response);
        }

        /**
         * Search for artworks by multiple criteria.
         * All parameters are optional; at least one should be provided.
         * Multiple criteria are combined with AND logic.
         * 
         * @param title    Search in artwork titles (optional)
         * @param location Search in location names (optional)
         * @param tags     Search in tags (optional)
         * @return List of artworks matching the criteria
         */
        /**
         * Search for artworks by multiple criteria.
         * All parameters are optional; at least one should be provided.
         * Multiple criteria are combined with AND logic.
         * Supports pagination.
         */
        @GetMapping("/artworks")
        public ResponseEntity<PagedResponse<ArtworkDto>> searchArtworks(
                        @RequestParam(required = false) String title,
                        @RequestParam(required = false) String location,
                        @RequestParam(required = false) String tags,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        Authentication authentication) {

                Pageable pageable = PageRequest.of(page, size);
                Page<Artwork> artworks = searchService.searchArtworks(title, location, tags, pageable);

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
