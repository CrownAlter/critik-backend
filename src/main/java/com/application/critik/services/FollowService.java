package com.application.critik.services;

import com.application.critik.entities.Follow;
import com.application.critik.entities.User;
import com.application.critik.exceptions.DuplicateResourceException;
import com.application.critik.exceptions.ResourceNotFoundException;
import com.application.critik.repositories.FollowRepository;
import com.application.critik.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing follow relationships between users.
 * 
 * Features:
 * - Follow/unfollow users
 * - Get followers list
 * - Get following list
 * 
 * Security:
 * - Users can only follow/unfollow on their own behalf
 * - Cannot follow yourself
 */
@Service
@RequiredArgsConstructor
public class FollowService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    /**
     * Follow a user.
     * 
     * @param followerUsername Username of the user who wants to follow
     * @param followedId ID of the user to follow
     * @throws ResourceNotFoundException if users not found
     * @throws IllegalArgumentException if trying to follow yourself
     * @throws DuplicateResourceException if already following
     */
    public void followUser(String followerUsername, Long followedId) {
        User follower = userRepository.findByUsername(followerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User", followerUsername));
        User followed = userRepository.findById(followedId)
                .orElseThrow(() -> new ResourceNotFoundException("User", followedId));

        if (follower.getId().equals(followedId)) {
            throw new IllegalArgumentException("You cannot follow yourself");
        }

        if (followRepository.existsByFollowerIdAndFollowedId(follower.getId(), followedId)) {
            throw new DuplicateResourceException("You are already following this user");
        }

        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowed(followed);
        followRepository.save(follow);
    }

    /**
     * Unfollow a user.
     * 
     * @param followerUsername Username of the user who wants to unfollow
     * @param followedId ID of the user to unfollow
     * @throws ResourceNotFoundException if users not found
     */
    @Transactional
    public void unfollowUser(String followerUsername, Long followedId) {
        User follower = userRepository.findByUsername(followerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User", followerUsername));
        User followed = userRepository.findById(followedId)
                .orElseThrow(() -> new ResourceNotFoundException("User", followedId));

        followRepository.deleteByFollowerAndFollowed(follower, followed);
    }

    /**
     * Get all followers of a user.
     * 
     * @param userId ID of the user
     * @return List of users following this user
     */
    @Transactional
    public List<User> getFollowers(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return followRepository.findByFollowed(user).stream()
                .map(Follow::getFollower)
                .collect(Collectors.toList());
    }

    /**
     * Get all users that a user follows.
     * 
     * @param userId ID of the user
     * @return List of users this user follows
     */
    @Transactional
    public List<User> getFollowing(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return followRepository.findByFollower(user).stream()
                .map(Follow::getFollowed)
                .collect(Collectors.toList());
    }

    /**
     * Check if a user is following another user.
     * 
     * @param followerId ID of potential follower
     * @param followedId ID of potentially followed user
     * @return true if following, false otherwise
     */
    public boolean isFollowing(Long followerId, Long followedId) {
        return followRepository.existsByFollowerIdAndFollowedId(followerId, followedId);
    }
}
