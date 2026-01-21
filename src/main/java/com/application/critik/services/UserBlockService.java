package com.application.critik.services;

import com.application.critik.dto.PagedResponse;
import com.application.critik.dto.UserDto;
import com.application.critik.entities.User;
import com.application.critik.entities.UserBlock;
import com.application.critik.exceptions.DuplicateResourceException;
import com.application.critik.exceptions.ResourceNotFoundException;
import com.application.critik.exceptions.UnauthorizedException;
import com.application.critik.repositories.UserBlockRepository;
import com.application.critik.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing user blocking functionality.
 */
@Service
@RequiredArgsConstructor
public class UserBlockService {

    private final UserBlockRepository userBlockRepository;
    private final UserRepository userRepository;

    /**
     * Block a user.
     * 
     * @param blockerId ID of user doing the blocking
     * @param blockedId ID of user to be blocked
     * @throws ResourceNotFoundException  if either user not found
     * @throws UnauthorizedException      if trying to block yourself
     * @throws DuplicateResourceException if already blocked
     */
    @Transactional
    public void blockUser(Long blockerId, Long blockedId) {
        // Cannot block yourself
        if (blockerId.equals(blockedId)) {
            throw new UnauthorizedException("Cannot block yourself");
        }

        User blocker = userRepository.findById(blockerId)
                .orElseThrow(() -> new ResourceNotFoundException("Blocker user not found"));

        User blocked = userRepository.findById(blockedId)
                .orElseThrow(() -> new ResourceNotFoundException("User to block not found"));

        // Check if already blocked
        if (userBlockRepository.existsByBlockerAndBlocked(blocker, blocked)) {
            throw new DuplicateResourceException("User already blocked");
        }

        UserBlock userBlock = UserBlock.builder()
                .blocker(blocker)
                .blocked(blocked)
                .build();

        userBlockRepository.save(userBlock);
    }

    /**
     * Unblock a user.
     * 
     * @param blockerId ID of user doing the unblocking
     * @param blockedId ID of user to be unblocked
     * @throws ResourceNotFoundException if either user not found
     */
    @Transactional
    public void unblockUser(Long blockerId, Long blockedId) {
        User blocker = userRepository.findById(blockerId)
                .orElseThrow(() -> new ResourceNotFoundException("Blocker user not found"));

        User blocked = userRepository.findById(blockedId)
                .orElseThrow(() -> new ResourceNotFoundException("User to unblock not found"));

        userBlockRepository.deleteByBlockerAndBlocked(blocker, blocked);
    }

    /**
     * Check if a user has blocked another user.
     * 
     * @param blockerId Potential blocker user ID
     * @param blockedId Potential blocked user ID
     * @return true if blocked, false otherwise
     */
    public boolean isBlocked(Long blockerId, Long blockedId) {
        User blocker = userRepository.findById(blockerId).orElse(null);
        User blocked = userRepository.findById(blockedId).orElse(null);

        if (blocker == null || blocked == null) {
            return false;
        }

        return userBlockRepository.existsByBlockerAndBlocked(blocker, blocked);
    }

    /**
     * Check if there's a block relationship in either direction.
     * 
     * @param user1Id First user ID
     * @param user2Id Second user ID
     * @return true if either user has blocked the other
     */
    public boolean isBlockedEitherWay(Long user1Id, Long user2Id) {
        User user1 = userRepository.findById(user1Id).orElse(null);
        User user2 = userRepository.findById(user2Id).orElse(null);

        if (user1 == null || user2 == null) {
            return false;
        }

        return userBlockRepository.existsBlockBetween(user1, user2);
    }

    /**
     * Get list of users blocked by a user (paginated).
     * 
     * @param userId   User ID
     * @param pageable Pagination parameters
     * @return Paginated list of blocked users
     * @throws ResourceNotFoundException if user not found
     */
    public PagedResponse<UserDto> getBlockedUsers(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Page<UserBlock> blockPage = userBlockRepository.findByBlockerOrderByCreatedAtDesc(user, pageable);

        Page<UserDto> userDtoPage = blockPage.map(block -> mapToUserDto(block.getBlocked()));

        return PagedResponse.<UserDto>builder()
                .content(userDtoPage.getContent())
                .page(userDtoPage.getNumber())
                .size(userDtoPage.getSize())
                .totalElements(userDtoPage.getTotalElements())
                .totalPages(userDtoPage.getTotalPages())
                .last(userDtoPage.isLast())
                .first(userDtoPage.isFirst())
                .build();
    }

    /**
     * Get list of user IDs that the given user has blocked.
     * Used for filtering feeds and search results.
     * 
     * @param userId User ID
     * @return List of blocked user IDs
     */
    public List<Long> getBlockedUserIds(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return new ArrayList<>();
        }

        return userBlockRepository.findBlockedUserIds(user);
    }

    /**
     * Get list of user IDs that have blocked the given user.
     * Used for filtering interactions.
     * 
     * @param userId User ID
     * @return List of blocker user IDs
     */
    public List<Long> getBlockerUserIds(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return new ArrayList<>();
        }

        return userBlockRepository.findBlockerUserIds(user);
    }

    /**
     * Get combined list of user IDs to exclude from feeds.
     * Includes both users the given user has blocked and users who have blocked
     * them.
     * 
     * @param userId User ID
     * @return List of user IDs to exclude
     */
    public List<Long> getAllBlockedUserIds(Long userId) {
        List<Long> blockedIds = getBlockedUserIds(userId);
        List<Long> blockerIds = getBlockerUserIds(userId);

        // Combine both lists
        List<Long> allBlocked = new ArrayList<>(blockedIds);
        for (Long id : blockerIds) {
            if (!allBlocked.contains(id)) {
                allBlocked.add(id);
            }
        }

        // Always exclude the user's own ID to prevent issues
        if (!allBlocked.contains(userId)) {
            allBlocked.add(userId);
        }

        // If empty, add a dummy ID to prevent SQL errors
        if (allBlocked.isEmpty()) {
            allBlocked.add(-1L);
        }

        return allBlocked;
    }

    /**
     * Helper method to map User entity to UserDto.
     */
    private UserDto mapToUserDto(User user) {
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
}
