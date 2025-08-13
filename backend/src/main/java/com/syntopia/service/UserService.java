package com.syntopia.service;

import com.syntopia.model.User;
import com.syntopia.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * User Service for Syntopia Platform
 * 
 * Handles user management, authentication, and progression tracking
 * in the Sacred Geometry gamification system.
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Create a new user
     */
    public User createUser(String username, String email, String displayName) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }

        User user = new User(username, email);
        user.setDisplayName(displayName);
        return userRepository.save(user);
    }

    /**
     * Find user by username
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Find user by email
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Update user's last login time
     */
    public void updateLastLogin(String userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
        }
    }

    /**
     * Set user's selected role (one of the 7 initial roles)
     */
    public User setSelectedRole(String userId, String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        user.setSelectedRole(role);
        return userRepository.save(user);
    }

    /**
     * Add experience points to user
     */
    public User addExperiencePoints(String userId, long points) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        user.setExperiencePoints(user.getExperiencePoints() + points);
        
        // Check for level up (simple algorithm: 1000 XP per level)
        int newLevel = (int) (user.getExperiencePoints() / 1000) + 1;
        if (newLevel > user.getCurrentLevel() && newLevel <= 25) { // SCL max is 25
            user.setCurrentLevel(newLevel);
        }
        
        return userRepository.save(user);
    }

    /**
     * Complete a quest for user
     */
    public User completeQuest(String userId, long experienceReward) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        user.setQuestsCompleted(user.getQuestsCompleted() + 1);
        return addExperiencePoints(userId, experienceReward);
    }

    /**
     * Enable GitHub integration for user
     */
    public User enableGitHubIntegration(String userId, String githubId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        user.setGithubId(githubId);
        user.setGitHubIntegrated(true);
        return userRepository.save(user);
    }

    /**
     * Get leaderboard (top contributors)
     */
    public List<User> getLeaderboard() {
        return userRepository.findTop10ByOrderByExperiencePointsDesc();
    }

    /**
     * Get users by role
     */
    public List<User> getUsersByRole(String role) {
        return userRepository.findBySelectedRole(role);
    }

    /**
     * Get users ready for GitHub integration (Level 4+)
     */
    public List<User> getUsersReadyForGitHub() {
        return userRepository.findByCurrentLevelGreaterThanEqual(4);
    }

    /**
     * Add a security role to a user (e.g., ROLE_ADMIN)
     */
    public User addRoleToUser(String userId, String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        if (user.getRoles() == null) {
            user.setRoles(new java.util.HashSet<>());
        }
        
        user.getRoles().add(role);
        return userRepository.save(user);
    }

    /**
     * Save user
     */
    public User save(User user) {
        return userRepository.save(user);
    }

    // Retained for legacy calls in tests; core security now uses AuthenticationUserDetailsService
    public UserDetails loadUserByUsername(String username) {
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    String[] authorities = user.getRoles() != null && !user.getRoles().isEmpty()
        ? user.getRoles().toArray(new String[0])
        : new String[]{"ROLE_USER"};
    return org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
        .password(user.getPasswordHash() != null ? user.getPasswordHash() : "")
        .authorities(authorities)
        .accountExpired(false)
        .accountLocked(!user.isEnabled())
        .credentialsExpired(false)
        .disabled(!user.isEnabled())
        .build();
    }
}
