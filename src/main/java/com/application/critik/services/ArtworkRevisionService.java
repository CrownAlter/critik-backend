package com.application.critik.services;

import com.application.critik.dto.ArtworkRevisionDto;
import com.application.critik.entities.Artwork;
import com.application.critik.entities.ArtworkRevision;
import com.application.critik.exceptions.ResourceNotFoundException;
import com.application.critik.repositories.ArtworkRepository;
import com.application.critik.repositories.ArtworkRevisionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing artwork edit history.
 */
@Service
@RequiredArgsConstructor
public class ArtworkRevisionService {

    private final ArtworkRevisionRepository artworkRevisionRepository;
    private final ArtworkRepository artworkRepository;

    /**
     * Save the current state of an artwork as a revision before editing.
     * 
     * @param artwork Artwork to save revision for
     */
    @Transactional
    public void saveRevision(Artwork artwork) {
        ArtworkRevision revision = ArtworkRevision.builder()
                .artwork(artwork)
                .previousTitle(artwork.getTitle())
                .previousArtistName(artwork.getArtistName())
                .previousLocationName(artwork.getLocationName())
                .previousInterpretation(artwork.getInterpretation())
                .previousTags(artwork.getTags())
                .build();

        artworkRevisionRepository.save(revision);
    }

    /**
     * Get edit history for an artwork.
     * 
     * @param artworkId Artwork ID
     * @return List of revisions (newest first)
     * @throws ResourceNotFoundException if artwork not found
     */
    public List<ArtworkRevisionDto> getArtworkHistory(Long artworkId) {
        Artwork artwork = artworkRepository.findById(artworkId)
                .orElseThrow(() -> new ResourceNotFoundException("Artwork not found"));

        List<ArtworkRevision> revisions = artworkRevisionRepository.findByArtworkOrderByEditedAtDesc(artwork);

        return revisions.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get the number of times an artwork has been edited.
     * 
     * @param artworkId Artwork ID
     * @return Number of edits
     */
    public long getEditCount(Long artworkId) {
        Artwork artwork = artworkRepository.findById(artworkId).orElse(null);
        if (artwork == null) {
            return 0;
        }

        return artworkRevisionRepository.countByArtwork(artwork);
    }

    /**
     * Delete all revisions for an artwork (when artwork is deleted).
     * 
     * @param artwork Artwork entity
     */
    @Transactional
    public void deleteRevisions(Artwork artwork) {
        artworkRevisionRepository.deleteByArtwork(artwork);
    }

    /**
     * Helper method to map ArtworkRevision entity to DTO.
     */
    private ArtworkRevisionDto mapToDto(ArtworkRevision revision) {
        return ArtworkRevisionDto.builder()
                .id(revision.getId())
                .artworkId(revision.getArtwork().getId())
                .previousTitle(revision.getPreviousTitle())
                .previousArtistName(revision.getPreviousArtistName())
                .previousLocationName(revision.getPreviousLocationName())
                .previousInterpretation(revision.getPreviousInterpretation())
                .previousTags(revision.getPreviousTags())
                .editedAt(revision.getEditedAt())
                .build();
    }
}
