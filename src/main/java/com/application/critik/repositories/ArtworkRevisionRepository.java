package com.application.critik.repositories;

import com.application.critik.entities.ArtworkRevision;
import com.application.critik.entities.Artwork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArtworkRevisionRepository extends JpaRepository<ArtworkRevision, Long> {

    /**
     * Get all revisions for an artwork, ordered by edit time (newest first)
     */
    List<ArtworkRevision> findByArtworkOrderByEditedAtDesc(Artwork artwork);

    /**
     * Count total edits for an artwork
     */
    long countByArtwork(Artwork artwork);

    /**
     * Delete all revisions for an artwork (when artwork is deleted)
     */
    void deleteByArtwork(Artwork artwork);
}
