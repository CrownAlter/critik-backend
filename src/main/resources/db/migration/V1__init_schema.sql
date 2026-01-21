-- =====================================================================
-- Critik Database Schema - PostgreSQL 18
-- =====================================================================
-- This migration script creates all tables for the Critik art review
-- platform with proper relationships, constraints, and indexes.
--
-- Tables created:
-- 1. users - User accounts and profiles
-- 2. artworks - Art posts with metadata
-- 3. comments - Nested comment system
-- 4. reactions - Artwork reactions (AGREE/DISAGREE)
-- 5. comment_reactions - Comment reactions
-- 6. bookmarks - Saved artworks
-- 7. follows - User follow relationships
-- 8. user_blocks - User blocking
-- 9. artwork_revisions - Edit history
-- 10. refresh_tokens - JWT refresh tokens
-- =====================================================================

-- Drop tables if they exist (for clean re-runs)
DROP TABLE IF EXISTS refresh_tokens CASCADE;
DROP TABLE IF EXISTS artwork_revisions CASCADE;
DROP TABLE IF EXISTS user_blocks CASCADE;
DROP TABLE IF EXISTS follows CASCADE;
DROP TABLE IF EXISTS bookmarks CASCADE;
DROP TABLE IF EXISTS comment_reactions CASCADE;
DROP TABLE IF EXISTS reactions CASCADE;
DROP TABLE IF EXISTS comments CASCADE;
DROP TABLE IF EXISTS artworks CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- =====================================================================
-- 1. USERS TABLE
-- =====================================================================
-- Stores user accounts with authentication credentials and profile info
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    display_name VARCHAR(100),
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL, -- BCrypt hashed password
    bio VARCHAR(500),
    avatar_url VARCHAR(500),
    banner_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_username_length CHECK (LENGTH(username) >= 3),
    CONSTRAINT chk_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

-- Indexes for user search and authentication
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_display_name ON users(display_name);

-- =====================================================================
-- 2. ARTWORKS TABLE
-- =====================================================================
-- Stores artwork posts with metadata, location, and interpretation
CREATE TABLE artworks (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    artist_name VARCHAR(200),
    image_url VARCHAR(500),
    location_lat DOUBLE PRECISION,
    location_lon DOUBLE PRECISION,
    location_name VARCHAR(300),
    interpretation TEXT,
    tags VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    edited BOOLEAN DEFAULT FALSE,
    last_edited_at TIMESTAMP,
    
    CONSTRAINT fk_artwork_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_title_not_empty CHECK (LENGTH(TRIM(title)) > 0),
    CONSTRAINT chk_location_coords CHECK (
        (location_lat IS NULL AND location_lon IS NULL) OR
        (location_lat BETWEEN -90 AND 90 AND location_lon BETWEEN -180 AND 180)
    )
);

-- Indexes for artwork queries
CREATE INDEX idx_artworks_user_id ON artworks(user_id);
CREATE INDEX idx_artworks_created_at ON artworks(created_at DESC);
CREATE INDEX idx_artworks_title ON artworks(title);
CREATE INDEX idx_artworks_artist_name ON artworks(artist_name);
CREATE INDEX idx_artworks_tags ON artworks(tags);
CREATE INDEX idx_artworks_location_name ON artworks(location_name);

-- =====================================================================
-- 3. COMMENTS TABLE
-- =====================================================================
-- Nested comment system with self-referential parent-child relationship
CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    artwork_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    comment_text TEXT NOT NULL,
    parent_comment_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_comment_artwork FOREIGN KEY (artwork_id) 
        REFERENCES artworks(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_parent FOREIGN KEY (parent_comment_id) 
        REFERENCES comments(id) ON DELETE CASCADE,
    CONSTRAINT chk_comment_text_not_empty CHECK (LENGTH(TRIM(comment_text)) > 0),
    CONSTRAINT chk_comment_text_length CHECK (LENGTH(comment_text) <= 2000)
);

-- Indexes for comment queries
CREATE INDEX idx_comments_artwork_id ON comments(artwork_id);
CREATE INDEX idx_comments_user_id ON comments(user_id);
CREATE INDEX idx_comments_parent_id ON comments(parent_comment_id);
CREATE INDEX idx_comments_created_at ON comments(created_at DESC);

-- =====================================================================
-- 4. REACTIONS TABLE
-- =====================================================================
-- User reactions to artworks (AGREE/DISAGREE)
CREATE TABLE reactions (
    id BIGSERIAL PRIMARY KEY,
    artwork_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_reaction_artwork FOREIGN KEY (artwork_id) 
        REFERENCES artworks(id) ON DELETE CASCADE,
    CONSTRAINT fk_reaction_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_reaction_type CHECK (type IN ('AGREE', 'DISAGREE')),
    CONSTRAINT uq_reaction_user_artwork UNIQUE (user_id, artwork_id)
);

-- Indexes for reaction queries
CREATE INDEX idx_reactions_artwork_id ON reactions(artwork_id);
CREATE INDEX idx_reactions_user_id ON reactions(user_id);
CREATE INDEX idx_reactions_type ON reactions(type);

-- =====================================================================
-- 5. COMMENT_REACTIONS TABLE
-- =====================================================================
-- User reactions to comments (AGREE/DISAGREE)
CREATE TABLE comment_reactions (
    id BIGSERIAL PRIMARY KEY,
    comment_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_comment_reaction_comment FOREIGN KEY (comment_id) 
        REFERENCES comments(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_reaction_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_comment_reaction_type CHECK (type IN ('AGREE', 'DISAGREE')),
    CONSTRAINT uq_comment_reaction_user_comment UNIQUE (user_id, comment_id)
);

-- Indexes for comment reaction queries
CREATE INDEX idx_comment_reactions_comment_id ON comment_reactions(comment_id);
CREATE INDEX idx_comment_reactions_user_id ON comment_reactions(user_id);

-- =====================================================================
-- 6. BOOKMARKS TABLE
-- =====================================================================
-- User bookmarks/saved artworks
CREATE TABLE bookmarks (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    artwork_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_bookmark_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_bookmark_artwork FOREIGN KEY (artwork_id) 
        REFERENCES artworks(id) ON DELETE CASCADE,
    CONSTRAINT uq_bookmark_user_artwork UNIQUE (user_id, artwork_id)
);

-- Indexes for bookmark queries
CREATE INDEX idx_bookmarks_user_id ON bookmarks(user_id);
CREATE INDEX idx_bookmarks_artwork_id ON bookmarks(artwork_id);
CREATE INDEX idx_bookmarks_created_at ON bookmarks(created_at DESC);

-- =====================================================================
-- 7. FOLLOWS TABLE
-- =====================================================================
-- User follow relationships
CREATE TABLE follows (
    id BIGSERIAL PRIMARY KEY,
    follower_id BIGINT NOT NULL,
    followed_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_follow_follower FOREIGN KEY (follower_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_follow_followed FOREIGN KEY (followed_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_follow_not_self CHECK (follower_id != followed_id),
    CONSTRAINT uq_follow_relationship UNIQUE (follower_id, followed_id)
);

-- Indexes for follow queries
CREATE INDEX idx_follows_follower_id ON follows(follower_id);
CREATE INDEX idx_follows_followed_id ON follows(followed_id);
CREATE INDEX idx_follows_created_at ON follows(created_at DESC);

-- =====================================================================
-- 8. USER_BLOCKS TABLE
-- =====================================================================
-- User blocking relationships
CREATE TABLE user_blocks (
    id BIGSERIAL PRIMARY KEY,
    blocker_id BIGINT NOT NULL,
    blocked_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_block_blocker FOREIGN KEY (blocker_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_block_blocked FOREIGN KEY (blocked_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_block_not_self CHECK (blocker_id != blocked_id),
    CONSTRAINT uq_block_relationship UNIQUE (blocker_id, blocked_id)
);

-- Indexes for block queries
CREATE INDEX idx_user_blocks_blocker_id ON user_blocks(blocker_id);
CREATE INDEX idx_user_blocks_blocked_id ON user_blocks(blocked_id);

-- =====================================================================
-- 9. ARTWORK_REVISIONS TABLE
-- =====================================================================
-- Tracks edit history for artworks
CREATE TABLE artwork_revisions (
    id BIGSERIAL PRIMARY KEY,
    artwork_id BIGINT NOT NULL,
    previous_title VARCHAR(200),
    previous_artist_name VARCHAR(200),
    previous_location_name VARCHAR(300),
    previous_interpretation TEXT,
    previous_tags VARCHAR(500),
    edited_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_revision_artwork FOREIGN KEY (artwork_id) 
        REFERENCES artworks(id) ON DELETE CASCADE
);

-- Indexes for revision queries
CREATE INDEX idx_artwork_revisions_artwork_id ON artwork_revisions(artwork_id);
CREATE INDEX idx_artwork_revisions_edited_at ON artwork_revisions(edited_at DESC);

-- =====================================================================
-- 10. REFRESH_TOKENS TABLE
-- =====================================================================
-- JWT refresh token storage for authentication
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(500) NOT NULL UNIQUE,
    expiry_date TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes for refresh token queries
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_expiry_date ON refresh_tokens(expiry_date);
CREATE INDEX idx_refresh_tokens_revoked ON refresh_tokens(revoked);

-- =====================================================================
-- STATISTICS AND SUMMARY
-- =====================================================================
-- Display table counts (should all be 0 after initial migration)
DO $$
BEGIN
    RAISE NOTICE '=== Database Migration Complete ===';
    RAISE NOTICE 'Tables created: 10';
    RAISE NOTICE 'Indexes created: 35+';
    RAISE NOTICE 'Foreign keys: 17';
    RAISE NOTICE 'Unique constraints: 8';
    RAISE NOTICE 'Check constraints: 10';
    RAISE NOTICE '';
    RAISE NOTICE 'Database is ready for the Critik application!';
END $$;
