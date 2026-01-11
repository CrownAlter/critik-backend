package com.application.critik.services;

import com.application.critik.entities.Artwork;
import com.application.critik.entities.User;
import com.application.critik.repositories.ArtworkRepository;
import com.application.critik.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SearchService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArtworkRepository artworkRepository;

    public List<User> searchUsers(String query) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }

        Set<User> results = new HashSet<>();
        results.addAll(userRepository.findByUsernameContainingIgnoreCase(query));
        results.addAll(userRepository.findByDisplayNameContainingIgnoreCase(query));

        return new ArrayList<>(results);
    }

    public List<Artwork> searchArtworks(String title, String location, String tags) {

        if ((title == null || title.isBlank()) &&
                (location == null || location.isBlank()) &&
                (tags == null || tags.isBlank())) {
            return artworkRepository.findAll();
        }

        Set<Artwork> results = new HashSet<>();

        if (title != null && !title.isBlank()) {
            results.addAll(artworkRepository.findByTitleContainingIgnoreCase(title));
        }
        if (location != null && !location.isBlank()) {
            results.addAll(artworkRepository.findByLocationNameContainingIgnoreCase(location));
        }
        if (tags != null && !tags.isBlank()) {
            results.addAll(artworkRepository.findByTagsContainingIgnoreCase(tags));
        }

        return new ArrayList<>(results);
    }
}
