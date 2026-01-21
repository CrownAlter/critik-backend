package com.application.critik.mappers;

import com.application.critik.dto.ArtworkDto;
import com.application.critik.dto.UserDto;
import com.application.critik.entities.Artwork;
import com.application.critik.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ArtworkMapper {

    private final com.application.critik.repositories.BookmarkRepository bookmarkRepository;
    private final com.application.critik.repositories.ReactionRepository reactionRepository;

    public ArtworkDto toDto(Artwork artwork) {
        return toDto(artwork, null);
    }

    public ArtworkDto toDto(Artwork artwork, Long currentUserId) {
        boolean isBookmarked = false;
        com.application.critik.entities.ReactionType userReaction = null;

        if (currentUserId != null) {
            isBookmarked = bookmarkRepository.existsByUserIdAndArtworkId(currentUserId, artwork.getId());
            userReaction = reactionRepository.findByArtworkIdAndUserId(artwork.getId(), currentUserId)
                    .map(com.application.critik.entities.Reaction::getType)
                    .orElse(null);
        }

        // Calculate counts (could be optimized with dedicated repo methods or cached in
        // entity)
        long agreeCount = reactionRepository.countByArtworkIdAndType(artwork.getId(),
                com.application.critik.entities.ReactionType.AGREE);
        long disagreeCount = reactionRepository.countByArtworkIdAndType(artwork.getId(),
                com.application.critik.entities.ReactionType.DISAGREE);

        return ArtworkDto.builder()
                .id(artwork.getId())
                .user(toUserDto(artwork.getUser()))
                .title(artwork.getTitle())
                .artistName(artwork.getArtistName())
                .imageUrl(artwork.getImageUrl())
                .locationLat(artwork.getLocationLat())
                .locationLon(artwork.getLocationLon())
                .locationName(artwork.getLocationName())
                .interpretation(artwork.getInterpretation())
                .tags(splitTags(artwork.getTags()))
                .createdAt(artwork.getCreatedAt())
                .updatedAt(artwork.getUpdatedAt())
                .edited(artwork.isEdited())
                .lastEditedAt(artwork.getLastEditedAt())
                .isBookmarked(isBookmarked)
                .userReaction(userReaction)
                .agreeCount(agreeCount)
                .disagreeCount(disagreeCount)
                .build();
    }

    private UserDto toUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .email(user.getEmail())
                .bio(user.getBio())
                .avatarUrl(user.getAvatarUrl())
                .bannerUrl(user.getBannerUrl())
                .build();
    }

    private List<String> splitTags(String tags) {
        if (tags == null || tags.isBlank())
            return List.of();
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .toList();
    }
}
