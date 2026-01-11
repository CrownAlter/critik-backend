package com.application.critik.mappers;

import com.application.critik.dto.ArtworkDto;
import com.application.critik.dto.UserDto;
import com.application.critik.entities.Artwork;
import com.application.critik.entities.User;

import java.util.Arrays;
import java.util.List;

public class ArtworkMapper {

    public static ArtworkDto toDto(Artwork artwork) {
        return ArtworkDto.builder()
                .id(artwork.getId())
                .user(toUserDto(artwork.getUser()))
                .title(artwork.getTitle())
                .imageUrl(artwork.getImageUrl())
                .locationLat(artwork.getLocationLat())
                .locationLon(artwork.getLocationLon())
                .locationName(artwork.getLocationName())
                .interpretation(artwork.getInterpretation())
                .tags(splitTags(artwork.getTags()))
                .createdAt(artwork.getCreatedAt())
                .build();
    }

    private static UserDto toUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .email(user.getEmail())
                .bio(user.getBio())
                .build();
    }

    private static List<String> splitTags(String tags) {
        if (tags == null || tags.isBlank()) return List.of();
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .toList();
    }
}
